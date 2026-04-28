package com.logsage.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Idempotency marker — stores the hash of already-processed messages.
 *
 * Stage 3: Prevents duplicate processing when Kafka redelivers messages.
 * The SHA-256 hash of (service + timestamp + message) serves as the primary key.
 * Checking existsById(hash) is a single index lookup — O(1) in practice.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "processed_messages")
public class ProcessedMessageEntity {

    /**
     * SHA-256 hash of (service + timestamp + message).
     * This IS the primary key — no auto-generated ID needed.
     */
    @Id
    @Column(length = 64)
    private String messageHash;

    @Column(nullable = false)
    private LocalDateTime processedAt;
}
