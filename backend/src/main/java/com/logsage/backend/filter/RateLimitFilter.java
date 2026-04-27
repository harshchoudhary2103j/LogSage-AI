package com.logsage.backend.filter;

import com.logsage.backend.exception.RateLimitExceededException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiter for the /analyze endpoint.
 *
 * WHY (Fix #9): Each AI analysis call costs money (LLM API tokens) and
 * takes 5-30 seconds. Without rate limiting, an aggressive client could:
 * - Exhaust the AI API quota in minutes
 * - Fill the async executor queue, blocking legitimate requests
 *
 * STRATEGY: Fixed window rate limiting per IP address.
 * Default: 10 requests per 60 seconds per IP.
 * Simple and effective for Phase 1.
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${rate-limit.requests-per-window:10}")
    private int maxRequests;

    @Value("${rate-limit.window-ms:60000}")
    private long windowMs;

    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Only rate-limit the /analyze endpoint
        if (!request.getRequestURI().startsWith("/api/analyze")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        WindowCounter counter = counters.compute(clientIp, (ip, existing) -> {
            long now = System.currentTimeMillis();
            if (existing == null || existing.isExpired(now, windowMs)) {
                return new WindowCounter(now);
            }
            return existing;
        });

        int currentCount = counter.count.incrementAndGet();

        if (currentCount > maxRequests) {
            log.warn("Rate limit exceeded for IP: {} ({}/{} requests)", clientIp, currentCount, maxRequests);
            throw new RateLimitExceededException(
                    "Rate limit exceeded. Maximum " + maxRequests + " analysis requests per minute.");
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Simple fixed-window counter. Resets after windowMs elapses.
     */
    private static class WindowCounter {
        final long windowStart;
        final AtomicInteger count;

        WindowCounter(long windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(0);
        }

        boolean isExpired(long now, long windowMs) {
            return (now - windowStart) >= windowMs;
        }
    }
}
