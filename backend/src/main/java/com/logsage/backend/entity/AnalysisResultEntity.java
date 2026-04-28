package com.logsage.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA entity for AI analysis results.
 *
 * Stage 3: Replaces AnalysisResultStore (in-memory) with PostgreSQL.
 * Maps to the 'analysis_results' table.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "analysis_results")
public class AnalysisResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String service;

    private String errorType;

    @Column(columnDefinition = "TEXT")
    private String rootCause;

    private String severity;

    @Column(columnDefinition = "TEXT")
    private String fixSuggestion;

    /**
     * Database ID of the original LogEntryEntity.
     */
    @Column(name = "log_id", nullable = false)
    private Long logId;

    /**
     * SHA-256 hash of the original log entry (service + timestamp + message).
     * Enables O(1) idempotency checks before processing.
     */
    @Column(nullable = false)
    private String logHash;

    @Column(nullable = false)
    private LocalDateTime analyzedAt;

    @PrePersist
    protected void onCreate() {
        if (analyzedAt == null) {
            analyzedAt = LocalDateTime.now();
        }
    }
}
