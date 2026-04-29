package com.logsage.backend.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Custom Logback Appender that forwards log events to the LogSage AI backend in batches.
 *
 * Design decisions:
 * - Uses a bounded ArrayBlockingQueue to safely batch logs in memory without OOM risks.
 * - Flushes logs based on batch size OR a scheduled interval.
 * - Uses Java 11+ HttpClient with a dedicated virtual thread executor.
 * - Filters out its own HTTP/Kafka/Controller logs to prevent infinite feedback loops.
 * - Wraps everything in try-catch so a LogSage outage NEVER crashes the service.
 */
public class LogSageAppender extends AppenderBase<ILoggingEvent> {

    // ── Configurable from logback-spring.xml ──────────────────────────────────
    private String logsageUrl = "http://localhost:8081/api/logs";
    private String serviceName = "unknown-service";
    private int batchSize = 50;
    private int flushIntervalMs = 200;

    // ── Internal ──────────────────────────────────────────────────────────────
    private static final DateTimeFormatter ISO = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneOffset.UTC);

    // Loggers that must be silenced to prevent infinite loops:
    private static final Set<String> SUPPRESSED_LOGGERS = Set.of(
            "com.logsage.backend.logging.LogSageAppender",
            "com.logsage.backend.controller.LogController",
            "com.logsage.backend.kafka.LogProducer",
            "com.logsage.backend.kafka.LogConsumer",
            "reactor.netty",
            "io.netty"
    );

    // Max capacity to prevent OutOfMemory if the downstream service is unavailable
    private final BlockingQueue<String> logQueue = new ArrayBlockingQueue<>(10000);

    // Single thread to periodically trigger flush
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // Virtual threads for actual HTTP execution (non-blocking, highly concurrent)
    private final ExecutorService httpExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .executor(httpExecutor)
            .build();

    // ── AppenderBase lifecycle ────────────────────────────────────────────────

    @Override
    public void start() {
        super.start();
        // Schedule periodic background flush
        scheduler.scheduleAtFixedRate(this::flush, flushIntervalMs, flushIntervalMs, TimeUnit.MILLISECONDS);
        addInfo("LogSageAppender started → endpoint: " + logsageUrl + " | service: " + serviceName + " | batchSize: " + batchSize);
    }

    @Override
    public void stop() {
        // Shutdown scheduler first to prevent new flushes
        scheduler.shutdown();
        
        // Final synchronous flush to ensure no logs are lost on shutdown
        flush(); 
        
        // Shutdown HTTP executor
        httpExecutor.shutdownNow();
        super.stop();
    }

    // ── Core append logic ─────────────────────────────────────────────────────

    @Override
    protected void append(ILoggingEvent event) {
        // Guard 1: Never forward our own appender logs (infinite loop prevention)
        String loggerName = event.getLoggerName();
        for (String suppressed : SUPPRESSED_LOGGERS) {
            if (loggerName != null && loggerName.startsWith(suppressed)) {
                return;
            }
        }

        // Guard 2: Map Logback level to LogSage level; skip TRACE (not in our enum)
        String logSageLevel = mapLevel(event.getLevel().toString());
        if (logSageLevel == null) {
            return;
        }

        String timestamp = ISO.format(Instant.ofEpochMilli(event.getTimeStamp()));
        String message = escape(event.getFormattedMessage());

        // Create individual JSON object
        String logObj = "{\"service\":\"" + serviceName + "\"," +
                "\"level\":\"" + logSageLevel + "\"," +
                "\"message\":\"" + message + "\"," +
                "\"timestamp\":\"" + timestamp + "\"}";

        // Try to add to queue. If full, fail fast and drop (non-blocking)
        if (!logQueue.offer(logObj)) {
            return;
        }

        // If batch threshold is reached, trigger an async flush immediately
        if (logQueue.size() >= batchSize) {
            httpExecutor.submit(this::flush);
        }
    }

    // ── Batch Flush ───────────────────────────────────────────────────────────

    private void flush() {
        List<String> batch = new ArrayList<>(batchSize);
        // drainTo is thread-safe and atomic
        logQueue.drainTo(batch, batchSize);

        if (batch.isEmpty()) {
            return;
        }

        String payload = "[" + String.join(",", batch) + "]";
        
        // Fire-and-forget HTTP request via virtual thread
        httpExecutor.submit(() -> sendToLogsage(payload));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void sendToLogsage(String jsonBody) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(logsageUrl))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(3))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // sendAsync → non-blocking; we discard the response on success
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .exceptionally(ex -> {
                        // LogSage is down — silently fail (don't log, would cause recursion)
                        return null;
                    });
        } catch (Exception ex) {
            // Any other error (URI invalid, etc.) — silently fail
        }
    }

    /**
     * Maps Logback level strings to LogSage enum values.
     * Returns null for levels we don't support (e.g. TRACE, ALL, OFF).
     */
    private String mapLevel(String logbackLevel) {
        return switch (logbackLevel.toUpperCase()) {
            case "INFO"  -> "INFO";
            case "WARN"  -> "WARN";
            case "ERROR" -> "ERROR";
            case "DEBUG" -> "DEBUG";
            default      -> null; // TRACE, ALL, OFF → skip
        };
    }

    /**
     * Escapes characters that would break inline JSON strings.
     */
    private String escape(String raw) {
        if (raw == null) return "";
        return raw
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ── Logback XML property setters ──────────────────────────────────────────

    public void setLogsageUrl(String logsageUrl) {
        this.logsageUrl = logsageUrl;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setFlushIntervalMs(int flushIntervalMs) {
        this.flushIntervalMs = flushIntervalMs;
    }
}
