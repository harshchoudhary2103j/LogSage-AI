package com.logsage.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a single log entry from a microservice.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {

    @NotBlank(message = "Timestamp is required")
    private String timestamp;

    @NotBlank(message = "Service name is required")
    private String service;

    @NotNull(message = "Log level is required (INFO, WARN, ERROR)")
    private LogLevel level;

    @NotBlank(message = "Log message is required")
    private String message;
}
