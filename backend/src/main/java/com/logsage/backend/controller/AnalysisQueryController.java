package com.logsage.backend.controller;

import com.logsage.backend.entity.AnalysisResultEntity;
import com.logsage.backend.repository.AnalysisResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisQueryController {

    private final AnalysisResultRepository analysisResultRepository;

    @GetMapping("/{logId}")
    public ResponseEntity<AnalysisResultEntity> getAnalysisForLog(@PathVariable Long logId) {
        log.debug("Fetching analysis for logId: {}", logId);
        return analysisResultRepository.findByLogId(logId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
