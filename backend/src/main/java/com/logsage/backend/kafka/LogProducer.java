package com.logsage.backend.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logsage.backend.config.KafkaTopicConfig;
import com.logsage.backend.dto.LogEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Publishes log entries to the Kafka 'log-entries' topic.
 *
 * Each LogEntry is sent as a separate message, keyed by service name.
 * Keying by service ensures all logs from the same microservice
 * land in the same partition (preserving order per service).
 *
 * Uses async send with callback — does NOT block the HTTP request thread.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Publish a batch of log entries to Kafka.
     * Each entry becomes a separate Kafka message.
     *
     * @param logs list of log entries to publish
     */
    public void sendLogs(List<LogEntry> logs) {
        for (LogEntry entry : logs) {
            try {
                String json = objectMapper.writeValueAsString(entry);
                String key = entry.getService(); // partition key

                CompletableFuture<SendResult<String, String>> future =
                        kafkaTemplate.send(KafkaTopicConfig.LOG_ENTRIES_TOPIC, key, json);

                future.whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send log to Kafka [service={}]: {}",
                                entry.getService(), ex.getMessage());
                    } else {
                        log.debug("Log sent to Kafka [topic={}, partition={}, offset={}]",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });

            } catch (JsonProcessingException e) {
                log.error("Failed to serialize log entry: {}", e.getMessage());
            }
        }

        log.info("Published {} log entries to Kafka topic '{}'",
                logs.size(), KafkaTopicConfig.LOG_ENTRIES_TOPIC);
    }
}
