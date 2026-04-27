package com.logsage.backend.service;

import com.logsage.backend.dto.LogEntry;
import com.logsage.backend.store.InMemoryLogStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for log ingestion and retrieval.
 * Delegates storage to InMemoryLogStore.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {

    private final InMemoryLogStore logStore;

    /**
     * Store a batch of log entries.
     *
     * @param logs list of validated log entries
     * @return number of logs stored
     */
    public int storeLogs(List<LogEntry> logs) {
        logStore.saveAll(logs);
        log.info("Stored {} log entries. Total in store: {}", logs.size(), logStore.size());
        return logs.size();
    }

    /**
     * Retrieve all stored logs.
     */
    public List<LogEntry> getAllLogs() {
        return logStore.getAll();
    }

    /**
     * Retrieve logs for a specific service.
     */
    public List<LogEntry> getLogsByService(String service) {
        return logStore.getByService(service);
    }
}
