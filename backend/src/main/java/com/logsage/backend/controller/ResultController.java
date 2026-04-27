package com.logsage.backend.controller;

import com.logsage.backend.store.AnalysisResultStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for querying AI analysis results.
 *
 * Results are populated asynchronously by the Kafka consumer.
 * This endpoint lets the frontend poll for completed analyses.
 */
@Slf4j
@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class ResultController {

    private final AnalysisResultStore resultStore;

    /**
     * Get all analysis results, optionally filtered by service.
     */
    @GetMapping
    public ResponseEntity<List<AnalysisResultStore.StoredResult>> getResults(
            @RequestParam(required = false) String service) {

        List<AnalysisResultStore.StoredResult> results;

        if (service != null && !service.isBlank()) {
            results = resultStore.getByService(service);
            log.debug("Returning {} results for service: {}", results.size(), service);
        } else {
            results = resultStore.getAll();
            log.debug("Returning all {} results", results.size());
        }

        return ResponseEntity.ok(results);
    }
}
