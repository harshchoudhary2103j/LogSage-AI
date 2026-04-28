package com.logsage.backend.repository;

import com.logsage.backend.entity.ProcessedMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for idempotency tracking.
 *
 * The inherited existsById(String hash) method is all we need —
 * it performs a single PK index lookup to check if a message
 * has already been processed.
 */
public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessageEntity, String> {
    // existsById(hash) is inherited from JpaRepository
}
