package com.logsage.backend.controller;

import com.logsage.backend.dto.LogEntry;
import com.logsage.backend.dto.LogIngestionResponse;
import com.logsage.backend.kafka.LogProducer;
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

    /**
     * Accept a batch of log entries and publish to Kafka.
     *
     * Returns 202 Accepted — logs are queued, not yet processed.
     */
    @PostMapping
    public ResponseEntity<LogIngestionResponse> ingestLogs(
            @Valid @RequestBody List<@Valid LogEntry> logs) {
        log.info("Received {} log entries — publishing to Kafka", logs.size());

        logProducer.sendLogs(logs);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(new LogIngestionResponse("Logs queued for processing", logs.size()));
    }
}
