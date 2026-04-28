package com.logsage.backend.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logsage.backend.config.KafkaTopicConfig;
import com.logsage.backend.dto.LogEntry;
import com.logsage.backend.dto.LogLevel;
import com.logsage.backend.entity.LogEntryEntity;
import com.logsage.backend.repository.LogEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Consumes log entries from the Kafka 'log-entries' topic.
 *
 * Stage 3 changes:
 * 1. Replaced InMemoryLogStore with LogEntryRepository (PostgreSQL)
 * 2. JSON parse errors are logged and skipped (non-retryable)
 * 3. Kafka publishing errors for ERROR logs propagate naturally
 *
 * Responsibilities (unchanged from Stage 2):
 * - Stores ALL logs in the database
 * - Forwards ERROR logs to the 'analysis-requests' topic
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogConsumer {

    private final ObjectMapper objectMapper;
    private final LogEntryRepository logEntryRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(
            topics = KafkaTopicConfig.LOG_ENTRIES_TOPIC,
            groupId = "logsage-log-processors"
    )
    public void consume(String message) {
        LogEntry entry;

        // Step 1: Deserialize — invalid JSON is logged and skipped
        try {
            entry = objectMapper.readValue(message, LogEntry.class);
        } catch (JsonProcessingException e) {
            log.error("Invalid JSON in log-entries topic — skipping message: {}", e.getMessage());
            return; // Don't rethrow — malformed messages can't be fixed by retrying
        }

        log.info("Consumed log from Kafka [service={}, level={}]",
                entry.getService(), entry.getLevel());

        // Step 2: Persist to PostgreSQL
        LogEntryEntity entity = LogEntryEntity.builder()
                .service(entry.getService())
                .level(entry.getLevel())
                .timestamp(entry.getTimestamp())
                .message(entry.getMessage())
                .build();

        logEntryRepository.save(entity);

        // Step 3: Forward ERROR logs to analysis-requests topic
        if (entry.getLevel() == LogLevel.ERROR) {
            log.info("ERROR log detected — forwarding to analysis-requests [service={}]",
                    entry.getService());

            kafkaTemplate.send(
                    KafkaTopicConfig.ANALYSIS_REQUESTS_TOPIC,
                    entry.getService(),  // key = service name
                    message              // forward the same JSON
            );
        }
    }
}
