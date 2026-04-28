package com.logsage.backend.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logsage.backend.client.AiClient;
import com.logsage.backend.config.KafkaTopicConfig;
import com.logsage.backend.dto.AnalysisResponse;
import com.logsage.backend.dto.LogEntry;
import com.logsage.backend.entity.AnalysisResultEntity;
import com.logsage.backend.entity.ProcessedMessageEntity;
import com.logsage.backend.repository.AnalysisResultRepository;
import com.logsage.backend.repository.ProcessedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

/**
 * Dedicated AI analysis worker — consumes from 'analysis-requests' topic.
 *
 * Stage 3 changes:
 * 1. RETRY-AWARE: Exceptions propagate to Spring Kafka's error handler
 *    which retries 3x with exponential backoff (2s → 6s → 18s), then
 *    routes the message to the 'analysis-dlt' dead letter topic.
 *
 * 2. IDEMPOTENT: Computes SHA-256 hash of (service + timestamp + message)
 *    and checks the processed_messages table before processing. Kafka may
 *    redeliver messages after a crash — this prevents duplicate AI calls.
 *
 * 3. PERSISTENT: Results are stored in PostgreSQL (analysis_results table)
 *    instead of an in-memory ConcurrentHashMap that was lost on restart.
 *
 * 4. TRANSACTIONAL: Result save + idempotency marker save happen in the
 *    same DB transaction. If either fails, both roll back and the message
 *    will be retried by Kafka.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisWorker {

    private final ObjectMapper objectMapper;
    private final AiClient aiClient;
    private final AnalysisResultRepository analysisResultRepository;
    private final ProcessedMessageRepository processedMessageRepository;

    @KafkaListener(
            topics = KafkaTopicConfig.ANALYSIS_REQUESTS_TOPIC,
            groupId = "logsage-ai-workers",
            containerFactory = "retryableKafkaListenerContainerFactory"
    )
    @Transactional
    public void processAnalysisRequest(String message) throws JsonProcessingException {
        // Step 1: Deserialize — JsonProcessingException is non-retryable (goes straight to DLT)
        LogEntry entry = objectMapper.readValue(message, LogEntry.class);
        log.info("AnalysisWorker received request [service={}, message={}]",
                entry.getService(), entry.getMessage());

        // Step 2: Idempotency check — skip if already processed
        String hash = computeHash(entry);
        if (processedMessageRepository.existsById(hash)) {
            log.info("Duplicate detected — skipping [service={}, hash={}]",
                    entry.getService(), hash);
            return;
        }

        // Step 3: Call AI — AiAnalysisException is RETRYABLE (transient failure)
        // If this throws, Spring Kafka will retry with backoff, then DLT
        AnalysisResponse response = aiClient.analyze(List.of(entry));

        // Step 4: Persist result + mark as processed (same transaction)
        AnalysisResultEntity resultEntity = AnalysisResultEntity.builder()
                .service(entry.getService())
                .errorType(response.errorType())
                .rootCause(response.rootCause())
                .severity(response.severity())
                .fixSuggestion(response.fixSuggestion())
                .logHash(hash)
                .analyzedAt(LocalDateTime.now())
                .build();

        analysisResultRepository.save(resultEntity);
        processedMessageRepository.save(new ProcessedMessageEntity(hash, LocalDateTime.now()));

        log.info("AI analysis complete and persisted [service={}, severity={}, error_type={}, hash={}]",
                entry.getService(), response.severity(), response.errorType(), hash);
    }

    /**
     * Compute a SHA-256 hash of (service + timestamp + message).
     *
     * This creates a deterministic fingerprint for each unique log entry.
     * Two identical log entries (same service, timestamp, message) produce
     * the same hash → the second one is detected as a duplicate.
     */
    private String computeHash(LogEntry entry) {
        try {
            String raw = entry.getService() + "|" + entry.getTimestamp() + "|" + entry.getMessage();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available in all JVMs
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
