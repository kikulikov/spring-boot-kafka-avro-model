package io.confluent.admin;

import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BasicAdminComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAdminComponent.class);

    @Value("${application.topic.default.partitions}")
    private Integer defaultPartitions;

    @Value("${application.topic.default.replicas}")
    private Short defaultReplicas;

    @Value("${application.topic.books}")
    private String topicBooks;

    @Value("${application.topic.orders}")
    private String topicOrders;

    @Value("${application.topic.counts}")
    private String topicCounts;

    @Bean
    NewTopic topicBooks() {
        LOGGER.info("Makes sure that the topic `{}` exists using the Kafka Admin API", topicBooks);
        return new NewTopic(topicBooks, Optional.of(defaultPartitions), Optional.of(defaultReplicas));
    }

    @Bean
    NewTopic topicOrders() {
        LOGGER.info("Makes sure that the topic `{}` exists using the Kafka Admin API", topicOrders);
        return new NewTopic(topicOrders, Optional.of(defaultPartitions), Optional.of(defaultReplicas));
    }


    @Bean
    NewTopic topicCounts() {
        LOGGER.info("Makes sure that the topic `{}` exists using the Kafka Admin API", topicCounts);
        return new NewTopic(topicCounts, Optional.of(defaultPartitions), Optional.of(defaultReplicas));
    }
}

