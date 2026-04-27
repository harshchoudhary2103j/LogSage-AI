package com.logsage.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for the /analyze endpoint.
 * Wraps a list of log entries for batch analysis.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {

    @NotEmpty(message = "At least one log entry is required")
    @Valid
    private List<LogEntry> logs;
}
