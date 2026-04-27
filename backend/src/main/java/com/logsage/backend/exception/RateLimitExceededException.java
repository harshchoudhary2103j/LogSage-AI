package com.logsage.backend.exception;

/**
 * Thrown when a client exceeds the rate limit for AI analysis.
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}
