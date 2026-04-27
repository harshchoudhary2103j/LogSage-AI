package com.logsage.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson ObjectMapper configuration.
 *
 * WHY: ObjectMapper was previously inside AiClientConfig (SRP violation).
 * Separated into its own config class — single place to customize
 * JSON serialization behavior for the entire application.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
