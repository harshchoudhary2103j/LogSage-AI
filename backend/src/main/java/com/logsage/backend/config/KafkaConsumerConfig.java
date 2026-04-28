package com.logsage.backend.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.logsage.backend.exception.AiAnalysisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Headers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

import java.nio.charset.StandardCharsets;

/**
 * Kafka consumer configuration with retry and dead letter topic (DLT) support.
 *
 * Stage 3: Two listener container factories:
 *
 * 1. kafkaListenerContainerFactory (default)
 *    - Used by LogConsumer
 *    - Basic error handling, logs errors but does not route to DLT
 *    - Log storage failures should not pollute the AI analysis DLT
 *
 * 2. retryableKafkaListenerContainerFactory
 *    - Used by AnalysisWorker ONLY
 *    - 3 retries with exponential backoff (2s → 6s → 18s)
 *    - After all retries exhausted → message goes to analysis-dlt topic
 *    - Non-retryable exceptions (bad JSON, validation) skip retries → straight to DLT
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Default factory for LogConsumer — manual ack, no DLT routing.
     */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>>
    kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setConcurrency(3);
        return factory;
    }

    /**
     * Retry-enabled factory for AnalysisWorker.
     *
     * Retry policy:
     * - initialInterval: 2000ms (2 seconds)
     * - multiplier: 3.0  → 2s → 6s → 18s
     * - maxInterval: 18000ms (cap at 18 seconds)
     * - maxElapsedTime: calculated to allow exactly 3 retry attempts
     *
     * Non-retryable exceptions (sent straight to DLT):
     * - JsonProcessingException (malformed message — retrying won't help)
     * - IllegalArgumentException (validation failures)
     *
     * Retryable exceptions (transient — worth retrying):
     * - AiAnalysisException (LLM timeout, rate limit, network blip)
     */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>>
    retryableKafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setConcurrency(3);

        // DLT recoverer: routes failed messages to analysis-dlt
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> {
                    log.error("Routing to DLT after retries exhausted [topic={}, key={}, error={}]",
                            record.topic(), record.key(), ex.getMessage());
                    return new TopicPartition(KafkaTopicConfig.ANALYSIS_DLT_TOPIC, -1);
                }
        );

        // Exponential backoff: 2s → 6s → 18s (3 attempts)
        ExponentialBackOff backOff = new ExponentialBackOff();
        backOff.setInitialInterval(2000L);   // 2 seconds
        backOff.setMultiplier(3.0);           // 2s → 6s → 18s
        backOff.setMaxInterval(18000L);       // cap at 18s
        // maxElapsedTime controls total retry window.
        // 3 retries: 2s + 6s + 18s = 26s, set to 30s to allow some buffer
        backOff.setMaxElapsedTime(30000L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // Mark non-retryable exceptions — these go straight to DLT
        // (bad JSON or validation errors won't be fixed by retrying)
        errorHandler.addNotRetryableExceptions(
                JsonProcessingException.class,
                IllegalArgumentException.class
        );

        factory.setCommonErrorHandler(errorHandler);

        log.info("Configured retryable Kafka listener factory: 3 retries, exponential backoff (2s → 6s → 18s), DLT={}",
                KafkaTopicConfig.ANALYSIS_DLT_TOPIC);

        return factory;
    }
}
