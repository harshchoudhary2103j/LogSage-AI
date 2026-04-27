package com.logsage.backend.exception;

/**
 * Custom exception for AI analysis failures.
 * Thrown when the LLM call fails or returns unparseable responses.
 */
public class AiAnalysisException extends RuntimeException {

    public AiAnalysisException(String message) {
        super(message);
    }

    public AiAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
