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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Custom Logback Appender that forwards ALL log events to the LogSage AI backend.
 *
 * Design decisions:
 * - Uses Java 11+ HttpClient with a dedicated virtual thread executor → non-blocking, no Reactor dependency at Logback init time
 * - Filters out its own HTTP logs to prevent infinite feedback loops
 * - Wraps everything in try-catch so a LogSage outage NEVER crashes the service
 * - Reads service name, endpoint, and minimum level from logback-spring.xml <appender> properties
 */
public class LogSageAppender extends AppenderBase<ILoggingEvent> {

    // ── Configurable from logback-spring.xml ──────────────────────────────────
    private String logsageUrl = "http://localhost:8081/api/logs";
    private String serviceName = "unknown-service";

    // ── Internal ──────────────────────────────────────────────────────────────
    private static final DateTimeFormatter ISO = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneOffset.UTC);

    // Loggers that must be silenced to prevent infinite loops:
    // (The HttpClient itself uses java.net.http which doesn't go through Logback, so this
    //  mainly guards against any Logback internal logger calling append() recursively.)
    private static final Set<String> SUPPRESSED_LOGGERS = Set.of(
            "com.logsage.backend.logging.LogSageAppender",
            "com.logsage.backend.controller.LogController",
            "com.logsage.backend.kafka.LogProducer",
            "com.logsage.backend.kafka.LogConsumer",
            "reactor.netty",
            "io.netty"
    );

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .executor(executor)
            .build();

    // ── AppenderBase lifecycle ────────────────────────────────────────────────

    @Override
    public void start() {
        super.start();
        addInfo("LogSageAppender started → endpoint: " + logsageUrl + " | service: " + serviceName);
    }

    @Override
    public void stop() {
        super.stop();
        executor.shutdownNow();
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

        // Build the payload string manually — no Jackson dependency required here
        String timestamp = ISO.format(Instant.ofEpochMilli(event.getTimeStamp()));
        String message = escape(event.getFormattedMessage());

        String body = "[{\"service\":\"" + serviceName + "\"," +
                "\"level\":\"" + logSageLevel + "\"," +
                "\"message\":\"" + message + "\"," +
                "\"timestamp\":\"" + timestamp + "\"}]";

        // Fire-and-forget via virtual thread — never blocks the logging thread
        executor.submit(() -> sendToLogsage(body));
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
}
