package com.logsage.backend.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logsage.backend.client.AiClient;
import com.logsage.backend.config.KafkaTopicConfig;
import com.logsage.backend.dto.AnalysisResponse;
import com.logsage.backend.dto.LogEntry;
import com.logsage.backend.dto.LogLevel;
import com.logsage.backend.store.AnalysisResultStore;
import com.logsage.backend.store.InMemoryLogStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Consumes log entries from the Kafka 'log-entries' topic.
 *
 * Processing pipeline:
 * 1. Deserialize the JSON message → LogEntry
 * 2. Store ALL logs in InMemoryLogStore (for querying)
 * 3. Filter: only ERROR logs trigger AI analysis
 * 4. Call existing AiClient.analyze() (REUSED — not rewritten)
 * 5. Store the analysis result in AnalysisResultStore
 *
 * This consumer runs on its own thread (managed by Spring Kafka),
 * completely decoupled from HTTP request threads.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogConsumer {

    private final ObjectMapper objectMapper;
    private final AiClient aiClient;
    private final InMemoryLogStore logStore;
    private final AnalysisResultStore resultStore;

    @KafkaListener(
            topics = KafkaTopicConfig.LOG_ENTRIES_TOPIC,
            groupId = "logsage-processors"
    )
    public void consume(String message) {
        try {
            LogEntry entry = objectMapper.readValue(message, LogEntry.class);
            log.info("Consumed log from Kafka [service={}, level={}]",
                    entry.getService(), entry.getLevel());

            // Step 1: Store every log
            logStore.save(entry);

            // Step 2: Only analyze ERROR logs
            if (entry.getLevel() == LogLevel.ERROR) {
                log.info("ERROR log detected — triggering AI analysis for service: {}",
                        entry.getService());
                analyzeErrorLog(entry);
            }

        } catch (Exception e) {
            // Log and skip bad messages (don't block the consumer)
            log.error("Failed to process Kafka message: {}", e.getMessage(), e);
        }
    }

    /**
     * Call the existing AiClient to analyze an ERROR log.
     * Wraps the single entry in a list (AiClient expects List<LogEntry>).
     */
    private void analyzeErrorLog(LogEntry entry) {
        try {
            AnalysisResponse response = aiClient.analyze(List.of(entry));

            resultStore.save(entry.getService(), response);

            log.info("AI analysis complete [service={}, severity={}, error_type={}]",
                    entry.getService(), response.severity(), response.errorType());

        } catch (Exception e) {
            log.error("AI analysis failed for service {}: {}",
                    entry.getService(), e.getMessage());
            // Don't rethrow — consumer continues with next message
        }
    }
}
