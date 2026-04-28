package com.logsage.backend.repository;

import com.logsage.backend.entity.LogEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for log entries.
 * Replaces InMemoryLogStore.
 */
public interface LogEntryRepository extends JpaRepository<LogEntryEntity, Long> {

    List<LogEntryEntity> findByService(String service);

    List<LogEntryEntity> findAllByOrderByCreatedAtDesc();

    List<LogEntryEntity> findByLevelOrderByCreatedAtDesc(com.logsage.backend.dto.LogLevel level);

    List<LogEntryEntity> findByServiceOrderByCreatedAtDesc(String service);
}
