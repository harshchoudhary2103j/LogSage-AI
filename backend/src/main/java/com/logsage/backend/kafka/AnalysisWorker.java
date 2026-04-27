package com.logsage.backend.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logsage.backend.client.AiClient;
import com.logsage.backend.config.KafkaTopicConfig;
import com.logsage.backend.dto.AnalysisResponse;
import com.logsage.backend.dto.LogEntry;
import com.logsage.backend.store.AnalysisResultStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Dedicated AI analysis worker — consumes from 'analysis-requests' topic.
 *
 * Stage 2: This is the ONLY component that calls AiClient.
 * Separated from LogConsumer so that:
 * - Slow AI calls don't block log storage
 * - AI processing can be scaled independently (more workers)
 * - Log ingestion throughput is unaffected by AI latency
 *
 * Uses a separate consumer group so it processes independently.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisWorker {

    private final ObjectMapper objectMapper;
    private final AiClient aiClient;
    private final AnalysisResultStore resultStore;

    @KafkaListener(
            topics = KafkaTopicConfig.ANALYSIS_REQUESTS_TOPIC,
            groupId = "logsage-ai-workers"
    )
    public void processAnalysisRequest(String message) {
        try {
            LogEntry entry = objectMapper.readValue(message, LogEntry.class);
            log.info("AnalysisWorker received request [service={}, message={}]",
                    entry.getService(), entry.getMessage());

            // Call the existing AiClient (reused, not rewritten)
            AnalysisResponse response = aiClient.analyze(List.of(entry));

            // Store the result
            resultStore.save(entry.getService(), response);

            log.info("AI analysis complete [service={}, severity={}, error_type={}]",
                    entry.getService(), response.severity(), response.errorType());

        } catch (Exception e) {
            log.error("AnalysisWorker failed to process request: {}", e.getMessage(), e);
            // Don't rethrow — consumer continues with next message
        }
    }
}
