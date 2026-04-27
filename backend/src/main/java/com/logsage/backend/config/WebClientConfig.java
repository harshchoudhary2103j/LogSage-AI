package com.logsage.backend.config;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * WebClient configuration with proper timeouts.
 *
 * WHY: The original WebClient had ZERO timeouts — an unresponsive LLM API
 * would hang the thread indefinitely. Now we enforce:
 * - Connection timeout: 5s (fail fast if LLM is unreachable)
 * - Response timeout: 30s (LLM responses rarely exceed this)
 * - Max response size: 1MB (prevent OOM from unexpectedly large responses)
 */
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final AiProperties aiProperties;

    @Bean
    public WebClient aiWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, aiProperties.getConnectTimeoutMs())
                .responseTimeout(Duration.ofMillis(aiProperties.getReadTimeoutMs()));

        return WebClient.builder()
                .baseUrl(aiProperties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + aiProperties.getKey())
                .defaultHeader("Content-Type", "application/json")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(config -> config.defaultCodecs()
                        .maxInMemorySize(aiProperties.getMaxResponseSizeBytes()))
                .build();
    }
}
