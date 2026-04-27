package com.logsage.backend.dto;

/**
 * Response DTO for log ingestion — immutable record.
 *
 * WHY (Fix #7): The controller was returning a raw Map<String, Object>,
 * losing compile-time safety and making the API contract unclear.
 * A typed DTO ensures consistent serialization and is self-documenting.
 */
public record LogIngestionResponse(
        String message,
        int count
) {

    public static LogIngestionResponse success(int count) {
        return new LogIngestionResponse("Logs stored successfully", count);
    }
}
