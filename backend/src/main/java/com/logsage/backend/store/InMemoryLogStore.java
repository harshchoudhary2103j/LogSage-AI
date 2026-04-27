package com.logsage.backend.store;

import com.logsage.backend.dto.LogEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe in-memory store with bounded capacity.
 *
 * WHY (Fix #8): The original store grew unbounded — every log entry was
 * kept forever. With high ingestion rates, this would cause OOM.
 *
 * SOLUTION: LRU-style eviction. When capacity is reached, oldest entries
 * per service are evicted first. Default max: 10,000 entries total.
 */
@Slf4j
@Component
public class InMemoryLogStore {

    private final int maxCapacity;
    private final AtomicInteger totalSize = new AtomicInteger(0);
    private final ConcurrentMap<String, LinkedList<LogEntry>> store = new ConcurrentHashMap<>();

    public InMemoryLogStore(@Value("${log-store.max-capacity:10000}") int maxCapacity) {
        this.maxCapacity = maxCapacity;
        log.info("InMemoryLogStore initialized with max capacity: {}", maxCapacity);
    }

    /**
     * Store a single log entry with eviction if at capacity.
     */
    public void save(LogEntry entry) {
        store.compute(entry.getService(), (key, list) -> {
            if (list == null) {
                list = new LinkedList<>();
            }
            list.addLast(entry);
            totalSize.incrementAndGet();
            return list;
        });

        // Evict oldest entries if over capacity
        evictIfNeeded();

        log.debug("Stored log entry for service: {}", entry.getService());
    }

    /**
     * Store multiple log entries.
     */
    public void saveAll(List<LogEntry> entries) {
        entries.forEach(this::save);
        log.info("Stored {} log entries (total: {})", entries.size(), totalSize.get());
    }

    /**
     * Retrieve all logs for a given service (defensive copy).
     */
    public List<LogEntry> getByService(String service) {
        LinkedList<LogEntry> list = store.get(service);
        if (list == null) return Collections.emptyList();
        synchronized (list) {
            return List.copyOf(list);
        }
    }

    /**
     * Retrieve all stored logs across all services (defensive copy).
     */
    public List<LogEntry> getAll() {
        List<LogEntry> allLogs = new ArrayList<>();
        store.forEach((service, list) -> {
            synchronized (list) {
                allLogs.addAll(list);
            }
        });
        return Collections.unmodifiableList(allLogs);
    }

    /**
     * Clear all stored logs.
     */
    public void clear() {
        store.clear();
        totalSize.set(0);
        log.info("Log store cleared");
    }

    public int size() {
        return totalSize.get();
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Evict oldest entries across all services when over capacity.
     * Simple round-robin eviction: removes the oldest entry from
     * the service with the most entries.
     */
    private void evictIfNeeded() {
        while (totalSize.get() > maxCapacity) {
            // Find the service with the most entries
            String largestService = null;
            int largestSize = 0;
            for (var entry : store.entrySet()) {
                int size;
                synchronized (entry.getValue()) {
                    size = entry.getValue().size();
                }
                if (size > largestSize) {
                    largestSize = size;
                    largestService = entry.getKey();
                }
            }

            if (largestService == null) break;

            LinkedList<LogEntry> list = store.get(largestService);
            if (list != null) {
                synchronized (list) {
                    if (!list.isEmpty()) {
                        list.removeFirst();
                        totalSize.decrementAndGet();
                    }
                }
            }
        }
    }
}
