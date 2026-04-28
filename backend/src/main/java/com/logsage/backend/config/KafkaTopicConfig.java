package com.logsage.backend.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka topic configuration.
 *
 * Stage 3: Three topics
 * - log-entries: raw log ingestion
 * - analysis-requests: ERROR logs forwarded for AI analysis
 * - analysis-dlt: dead letter topic for messages that failed all retries
 */
@Configuration
public class KafkaTopicConfig {

    public static final String LOG_ENTRIES_TOPIC = "log-entries";
    public static final String ANALYSIS_REQUESTS_TOPIC = "analysis-requests";
    public static final String ANALYSIS_DLT_TOPIC = "analysis-dlt";

    @Bean
    public NewTopic logEntriesTopic() {
        return TopicBuilder.name(LOG_ENTRIES_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic analysisRequestsTopic() {
        return TopicBuilder.name(ANALYSIS_REQUESTS_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic analysisDltTopic() {
        return TopicBuilder.name(ANALYSIS_DLT_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
