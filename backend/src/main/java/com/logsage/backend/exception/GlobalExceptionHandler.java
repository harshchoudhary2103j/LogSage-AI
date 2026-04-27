package com.logsage.backend.exception;

import com.logsage.backend.dto.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers.
 *
 * CHANGES:
 * - Added HttpMessageNotReadableException handler (malformed JSON)
 * - Added RateLimitExceededException handler (429)
 * - Added RejectedExecutionException handler (AI queue full → 503)
 * - Uses ApiErrorResponse.of() factory method (cleaner)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Validation errors (e.g. @NotBlank, @NotNull failures).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("Validation failed: {}", errors);

        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(400, "Validation failed: " + errors));
    }

    /**
     * Malformed JSON in request body.
     * Without this, Spring returns a generic 500 — confusing for API consumers.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON in request: {}", ex.getMessage());

        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(400, "Malformed JSON in request body"));
    }

    /**
     * AI analysis failures (LLM unreachable, unparseable response, timeout).
     */
    @ExceptionHandler(AiAnalysisException.class)
    public ResponseEntity<ApiErrorResponse> handleAiAnalysisException(AiAnalysisException ex) {
        log.error("AI analysis failed: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiErrorResponse.of(503, "AI analysis failed: " + ex.getMessage()));
    }

    /**
     * Rate limit exceeded (Fix #9).
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimit(RateLimitExceededException ex) {
        log.warn("Rate limit exceeded: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiErrorResponse.of(429, ex.getMessage()));
    }

    /**
     * AI executor queue full — too many concurrent analysis requests (Fix #2).
     */
    @ExceptionHandler(RejectedExecutionException.class)
    public ResponseEntity<ApiErrorResponse> handleQueueFull(RejectedExecutionException ex) {
        log.error("AI analysis queue full: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiErrorResponse.of(503, "AI analysis service is at capacity. Please retry later."));
    }

    /**
     * Catch-all for unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(500, "An unexpected error occurred"));
    }
}
