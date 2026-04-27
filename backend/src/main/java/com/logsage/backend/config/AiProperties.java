package com.logsage.backend.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Type-safe configuration properties for AI/LLM integration.
 *
 * WHY: Replaces scattered @Value annotations with a validated, type-safe
 * configuration class. Fails fast at startup if required properties are missing.
 * All values sourced from environment variables — no hardcoded secrets.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "ai.api")
public class AiProperties {

    @NotBlank(message = "AI API key must be set via AI_API_KEY environment variable")
    private String key;

    @NotBlank(message = "AI API base URL is required")
    private String baseUrl = "https://api.openai.com/v1";

    @NotBlank(message = "AI model name is required")
    private String model = "gpt-3.5-turbo";

    /** Connection timeout in milliseconds */
    private int connectTimeoutMs = 5000;

    /** Response/read timeout in milliseconds */
    private int readTimeoutMs = 30000;

    /** Maximum response size in bytes (prevents OOM from huge AI responses) */
    private int maxResponseSizeBytes = 1024 * 1024; // 1MB
}
