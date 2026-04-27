package com.logsage.backend.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logsage.backend.config.KafkaTopicConfig;
import com.logsage.backend.dto.LogEntry;
import com.logsage.backend.dto.LogLevel;
import com.logsage.backend.store.InMemoryLogStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Consumes log entries from the Kafka 'log-entries' topic.
 *
 * Stage 2 change: This consumer NO LONGER calls AiClient directly.
 * Instead it:
 * 1. Stores ALL logs in InMemoryLogStore
 * 2. Forwards ERROR logs to the 'analysis-requests' topic
 *
 * The AI call is now handled by AnalysisWorker — a separate consumer.
 * This follows single responsibility: LogConsumer = store + route.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogConsumer {

    private final ObjectMapper objectMapper;
    private final InMemoryLogStore logStore;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(
            topics = KafkaTopicConfig.LOG_ENTRIES_TOPIC,
            groupId = "logsage-log-processors"
    )
    public void consume(String message) {
        try {
            LogEntry entry = objectMapper.readValue(message, LogEntry.class);
            log.info("Consumed log from Kafka [service={}, level={}]",
                    entry.getService(), entry.getLevel());

            // Step 1: Store every log
            logStore.save(entry);

            // Step 2: Forward ERROR logs to analysis-requests topic
            if (entry.getLevel() == LogLevel.ERROR) {
                log.info("ERROR log detected — forwarding to analysis-requests [service={}]",
                        entry.getService());

                kafkaTemplate.send(
                        KafkaTopicConfig.ANALYSIS_REQUESTS_TOPIC,
                        entry.getService(),  // key = service name
                        message              // forward the same JSON
                );
            }

        } catch (Exception e) {
            log.error("Failed to process Kafka message: {}", e.getMessage(), e);
        }
    }
}
