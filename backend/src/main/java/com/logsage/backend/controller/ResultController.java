package com.logsage.backend.controller;

import com.logsage.backend.entity.AnalysisResultEntity;
import com.logsage.backend.repository.AnalysisResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for querying AI analysis results.
 *
 * Stage 3: Uses AnalysisResultRepository (PostgreSQL) instead of AnalysisResultStore.
 * Results are populated asynchronously by the AnalysisWorker (Kafka consumer → AI → DB).
 * This endpoint lets the frontend poll for completed analyses.
 */
@Slf4j
@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class ResultController {

    private final AnalysisResultRepository analysisResultRepository;

    /**
     * Get all analysis results, optionally filtered by service.
     * Results are ordered by most recent first.
     */
    @GetMapping
    public ResponseEntity<List<AnalysisResultEntity>> getResults(
            @RequestParam(required = false) String service) {

        List<AnalysisResultEntity> results;

        if (service != null && !service.isBlank()) {
            results = analysisResultRepository.findByServiceOrderByAnalyzedAtDesc(service);
            log.debug("Returning {} results for service: {}", results.size(), service);
        } else {
            results = analysisResultRepository.findAllByOrderByAnalyzedAtDesc();
            log.debug("Returning all {} results", results.size());
        }

        return ResponseEntity.ok(results);
    }
}
