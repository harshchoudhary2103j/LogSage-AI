package com.logsage.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async executor configuration for AI analysis calls.
 *
 * WHY (Fix #2): The original code called WebClient.block() directly on
 * Tomcat request threads. With 200 default Tomcat threads and AI calls
 * taking 5-30 seconds each, just 200 concurrent requests would exhaust
 * the entire thread pool and make the app unresponsive.
 *
 * SOLUTION: Dedicated bounded thread pool for AI calls. Tomcat threads
 * are released immediately while the AI call runs on this isolated pool.
 * If the pool is full, requests get a clear error instead of hanging.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "aiExecutor")
    public Executor aiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("ai-analysis-");
        executor.setRejectedExecutionHandler((r, e) -> {
            throw new java.util.concurrent.RejectedExecutionException(
                    "AI analysis queue is full. Please retry later.");
        });
        executor.initialize();
        return executor;
    }
}
