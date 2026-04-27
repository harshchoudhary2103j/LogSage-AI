package com.logsage.backend.store;

import com.logsage.backend.dto.AnalysisResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple in-memory store for AI analysis results.
 *
 * Stores results keyed by service name.
 * Phase 2 will replace this with PostgreSQL.
 */
@Slf4j
@Component
public class AnalysisResultStore {

    /** Wraps a result with metadata */
    public record StoredResult(
            String service,
            AnalysisResponse response,
            LocalDateTime analyzedAt
    ) {}

    private static final int MAX_RESULTS_PER_SERVICE = 100;
    private final ConcurrentMap<String, LinkedList<StoredResult>> store = new ConcurrentHashMap<>();

    /**
     * Save an analysis result for a service.
     */
    public void save(String service, AnalysisResponse response) {
        store.compute(service, (key, list) -> {
            if (list == null) list = new LinkedList<>();
            list.addLast(new StoredResult(service, response, LocalDateTime.now()));
            // Evict oldest if over limit
            while (list.size() > MAX_RESULTS_PER_SERVICE) {
                list.removeFirst();
            }
            return list;
        });
        log.debug("Stored analysis result for service: {}", service);
    }

    /**
     * Get all results for a service.
     */
    public List<StoredResult> getByService(String service) {
        LinkedList<StoredResult> list = store.get(service);
        if (list == null) return Collections.emptyList();
        synchronized (list) {
            return List.copyOf(list);
        }
    }

    /**
     * Get all results across all services.
     */
    public List<StoredResult> getAll() {
        List<StoredResult> all = new ArrayList<>();
        store.forEach((service, list) -> {
            synchronized (list) {
                all.addAll(list);
            }
        });
        return Collections.unmodifiableList(all);
    }

    public int size() {
        return store.values().stream().mapToInt(List::size).sum();
    }
}
