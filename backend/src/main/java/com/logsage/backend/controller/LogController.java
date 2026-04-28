package com.logsage.backend.controller;

import com.logsage.backend.dto.LogEntry;
import com.logsage.backend.dto.LogIngestionResponse;
import com.logsage.backend.dto.LogLevel;
import com.logsage.backend.entity.LogEntryEntity;
import com.logsage.backend.kafka.LogProducer;
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
 * CHANGED FOR KAFKA: Instead of storing logs directly, we publish
 * them to Kafka. The response is now 202 Accepted (not 201 Created),
 * signaling that logs are queued for asynchronous processing.
 */
@Slf4j
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogProducer logProducer;
    private final LogService logService;

    /**
     * Accept a batch of log entries, save them to the DB, and publish to Kafka.
     *
     * Returns 202 Accepted — logs are queued, not yet processed by AI.
     */
    @PostMapping
    public ResponseEntity<LogIngestionResponse> ingestLogs(
            @Valid @RequestBody List<@Valid LogEntry> logs) {
        log.info("Received {} log entries — saving to DB and publishing to Kafka", logs.size());

        // Stage 4: Save to DB first to generate IDs
        List<LogEntry> savedLogs = logService.saveLogs(logs);

        // Stage 4: Publish the logs (now containing IDs) to Kafka
        logProducer.sendLogs(savedLogs);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(new LogIngestionResponse("Logs saved and queued for processing", logs.size()));
    }

    /**
     * Retrieve stored logs for the dashboard.
     */
    @GetMapping
    public ResponseEntity<List<LogEntryEntity>> getLogs(
            @RequestParam(required = false) LogLevel level) {
        
        if (level != null) {
            return ResponseEntity.ok(logService.getLogsByLevel(level));
        }
        
        return ResponseEntity.ok(logService.getAllLogs());
    }
}
