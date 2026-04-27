package com.logsage.backend.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka topic configuration.
 *
 * Creates the 'log-entries' topic on startup if it doesn't exist.
 * 1 partition + 1 replica — kept simple for Stage 1 debugging.
 */
@Configuration
public class KafkaTopicConfig {

    public static final String LOG_ENTRIES_TOPIC = "log-entries";

    @Bean
    public NewTopic logEntriesTopic() {
        return TopicBuilder.name(LOG_ENTRIES_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
