package com.logsage.backend.service;

import com.logsage.backend.dto.LogEntry;
import com.logsage.backend.entity.LogEntryEntity;
import com.logsage.backend.repository.LogEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for log retrieval.
 *
 * Stage 3: Delegates to LogEntryRepository (PostgreSQL) instead of InMemoryLogStore.
 * Note: Log storage is now handled by LogConsumer (Kafka consumer → DB).
 * This service provides the query API for REST endpoints.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {

    private final LogEntryRepository logEntryRepository;

    /**
     * Retrieve all stored logs, ordered by most recent first.
     */
    public List<LogEntryEntity> getAllLogs() {
        return logEntryRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Retrieve logs for a specific service, ordered by most recent first.
     */
    public List<LogEntryEntity> getLogsByService(String service) {
        return logEntryRepository.findByServiceOrderByCreatedAtDesc(service);
    }

    /**
     * Get total count of stored logs.
     */
    public long getLogCount() {
        return logEntryRepository.count();
    }
}
