package com.logsage.backend.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logsage.backend.config.AiProperties;
import com.logsage.backend.dto.AnalysisResponse;
import com.logsage.backend.dto.LogEntry;
import com.logsage.backend.exception.AiAnalysisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Client responsible for communicating with the LLM API.
 *
 * CHANGES FROM REVIEW:
 * - Fix #1: Added timeout to .block() call (defense in depth alongside WebClient timeout)
 * - Fix #3: Removed silent error swallowing — parse failures now throw AiAnalysisException
 * - Fix #5: Uses PromptBuilder instead of hardcoded prompt
 * - Fix #10: Timeout prevents indefinite blocking
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiClient {

    private final WebClient aiWebClient;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final PromptBuilder promptBuilder;

    /**
     * Send logs to the LLM API and return structured analysis.
     *
     * @param logs list of log entries to analyze
     * @return parsed AnalysisResponse
     * @throws AiAnalysisException if the API call or response parsing fails
     */
    public AnalysisResponse analyze(List<LogEntry> logs) {
        log.info("Sending {} log entries to AI for analysis", logs.size());

        // Build the OpenAI-compatible chat completion request
        Map<String, Object> requestBody = Map.of(
                "model", aiProperties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", promptBuilder.getSystemPrompt()),
                        Map.of("role", "user", "content", promptBuilder.buildUserPrompt(logs))
                ),
                "temperature", 0.3
        );

        try {
            // Fix #1 & #10: Timeout on block() as defense-in-depth
            // (WebClient also has its own timeout, but this is the last line of defense)
            String responseBody = aiWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(aiProperties.getReadTimeoutMs()))
                    .block(Duration.ofMillis(aiProperties.getReadTimeoutMs() + 1000));

            log.debug("Raw AI response: {}", responseBody);
            return parseAiResponse(responseBody);

        } catch (WebClientResponseException e) {
            log.error("AI API returned error status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiAnalysisException("AI API returned error: " + e.getStatusCode(), e);
        } catch (AiAnalysisException e) {
            throw e; // Don't wrap our own exceptions
        } catch (Exception e) {
            log.error("Failed to call AI API: {}", e.getMessage(), e);
            throw new AiAnalysisException("Failed to communicate with AI service: " + e.getMessage(), e);
        }
    }

    /**
     * Parse the AI response into an AnalysisResponse.
     *
     * Fix #3: NO MORE SILENT FAILURE. If parsing fails, we throw AiAnalysisException
     * which returns a proper HTTP 503 to the client. The old code returned a fake
     * "PARSE_ERROR" response with HTTP 200 — misleading and incorrect.
     */
    private AnalysisResponse parseAiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");

            if (choices.isEmpty() || choices.isMissingNode()) {
                throw new AiAnalysisException("AI response contained no choices");
            }

            String content = choices.get(0).path("message").path("content").asText();
            log.debug("Extracted AI content: {}", content);

            String cleanedJson = cleanJsonResponse(content);

            return objectMapper.readValue(cleanedJson, AnalysisResponse.class);

        } catch (AiAnalysisException e) {
            throw e;
        } catch (Exception e) {
            // Fix #3: Throw instead of returning fake data
            log.error("Failed to parse AI response: {}", e.getMessage(), e);
            throw new AiAnalysisException("AI returned an unparseable response: " + e.getMessage(), e);
        }
    }

    /**
     * Strip markdown code block wrappers that LLMs sometimes add.
     */
    private String cleanJsonResponse(String content) {
        String cleaned = content.trim();

        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            if (firstNewline != -1) {
                cleaned = cleaned.substring(firstNewline + 1);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();
        }

        return cleaned;
    }
}
