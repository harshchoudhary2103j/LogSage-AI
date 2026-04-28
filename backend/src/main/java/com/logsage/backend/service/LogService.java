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
     * Persists logs to the database and populates their generated IDs.
     * Stage 4: Logs are saved BEFORE being published to Kafka.
     */
    public List<LogEntry> saveLogs(List<LogEntry> logs) {
        List<LogEntryEntity> entities = logs.stream()
                .map(dto -> LogEntryEntity.builder()
                        .service(dto.getService())
                        .level(dto.getLevel())
                        .timestamp(dto.getTimestamp())
                        .message(dto.getMessage())
                        .build())
                .toList();

        List<LogEntryEntity> savedEntities = logEntryRepository.saveAll(entities);

        for (int i = 0; i < logs.size(); i++) {
            logs.get(i).setId(savedEntities.get(i).getId());
        }

        return logs;
    }

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
     * Retrieve logs for a specific level, ordered by most recent first.
     */
    public List<LogEntryEntity> getLogsByLevel(com.logsage.backend.dto.LogLevel level) {
        return logEntryRepository.findByLevelOrderByCreatedAtDesc(level);
    }

    /**
     * Get total count of stored logs.
     */
    public long getLogCount() {
        return logEntryRepository.count();
    }
}
