package com.logsage.backend.controller;

import com.logsage.backend.dto.LogEntry;
import com.logsage.backend.dto.LogIngestionResponse;
import com.logsage.backend.service.LogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for log ingestion.
 *
 * CHANGE (Fix #7): Returns typed LogIngestionResponse instead of raw Map.
 */
@Slf4j
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    /**
     * Accept and store a batch of log entries.
     */
    @PostMapping
    public ResponseEntity<LogIngestionResponse> ingestLogs(
            @Valid @RequestBody List<@Valid LogEntry> logs) {
        log.info("Received {} log entries for ingestion", logs.size());

        int stored = logService.storeLogs(logs);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(LogIngestionResponse.success(stored));
    }

    /**
     * Retrieve all stored logs (useful for debugging).
     */
    @GetMapping
    public ResponseEntity<List<LogEntry>> getAllLogs() {
        return ResponseEntity.ok(logService.getAllLogs());
    }
}
