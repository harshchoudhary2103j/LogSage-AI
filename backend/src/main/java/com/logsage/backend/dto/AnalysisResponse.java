package com.logsage.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Structured response from AI analysis — immutable record.
 *
 * WHY (Fix #6): The original used Lombok @Data which generates setters,
 * making it mutable. Response DTOs should be immutable value objects.
 * Java records are immutable by design and reduce boilerplate.
 *
 * @JsonProperty serves dual purpose: reads snake_case from LLM,
 * writes snake_case to our API response (consistent contract).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalysisResponse(
        @JsonProperty("error_type") String errorType,
        @JsonProperty("root_cause") String rootCause,
        @JsonProperty("severity") String severity,
        @JsonProperty("fix_suggestion") String fixSuggestion
) {
}
