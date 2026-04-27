package com.logsage.backend.controller;

import com.logsage.backend.dto.AnalysisRequest;
import com.logsage.backend.dto.AnalysisResponse;
import com.logsage.backend.service.AnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * REST controller for log analysis.
 *
 * CHANGE: Now returns CompletableFuture to leverage the async analysis.
 * Spring MVC automatically handles CompletableFuture return types —
 * the Tomcat thread is released while the AI call runs on the aiExecutor.
 */
@Slf4j
@RestController
@RequestMapping("/api/analyze")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    /**
     * Analyze the provided logs using AI (async).
     *
     * @param request contains the list of log entries to analyze
     * @return async structured AI analysis response
     */
    @PostMapping
    public CompletableFuture<ResponseEntity<AnalysisResponse>> analyzeLogs(
            @Valid @RequestBody AnalysisRequest request) {
        log.info("Analysis requested for {} log entries", request.getLogs().size());

        return analysisService.analyzeLogs(request.getLogs())
                .thenApply(ResponseEntity::ok);
    }
}
