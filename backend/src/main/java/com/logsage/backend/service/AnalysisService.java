package com.logsage.backend.service;

import com.logsage.backend.client.AiClient;
import com.logsage.backend.dto.AnalysisResponse;
import com.logsage.backend.dto.LogEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service layer that orchestrates log analysis.
 *
 * WHY @Async (Fix #2): The original code ran the AI call synchronously on
 * Tomcat request threads. Each call blocks for 5-30 seconds. With the default
 * 200 Tomcat threads, just 200 concurrent requests would exhaust the pool.
 *
 * SOLUTION: @Async runs the AI call on a dedicated bounded thread pool ("aiExecutor").
 * The Tomcat thread is released immediately via CompletableFuture.
 * If the executor queue is full, a clear error is returned (not silent hang).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AiClient aiClient;

    /**
     * Analyze log entries asynchronously on the dedicated AI executor.
     *
     * @param logs list of log entries to analyze
     * @return CompletableFuture containing the structured analysis
     */
    @Async("aiExecutor")
    public CompletableFuture<AnalysisResponse> analyzeLogs(List<LogEntry> logs) {
        log.info("Starting analysis for {} log entries on thread: {}",
                logs.size(), Thread.currentThread().getName());

        AnalysisResponse response = aiClient.analyze(logs);

        log.info("Analysis complete — error_type: {}, severity: {}",
                response.errorType(), response.severity());

        return CompletableFuture.completedFuture(response);
    }
}
