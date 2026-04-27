package com.logsage.backend.dto;

import java.time.LocalDateTime;

/**
 * Standard error envelope — immutable record.
 *
 * WHY (Fix #6): Same reasoning as AnalysisResponse. Error responses
 * should never be mutated after construction.
 */
public record ApiErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp
) {

    /**
     * Factory method for convenient construction with auto-timestamp.
     */
    public static ApiErrorResponse of(int status, String message) {
        return new ApiErrorResponse(status, message, LocalDateTime.now());
    }
}
