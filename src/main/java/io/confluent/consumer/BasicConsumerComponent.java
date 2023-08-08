package io.confluent.consumer;

import io.confluent.model.avro.Book;
import io.confluent.model.avro.Count;
import io.confluent.model.avro.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class BasicConsumerComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicConsumerComponent.class);

    @KafkaListener(topics = "${application.topic.books}")
    @SuppressWarnings("unused")
    public void receive(@Payload Book book) {
        // process the received record accordingly
        LOGGER.info("Received the Book '{}'", book.toString());
    }

    @KafkaListener(topics = "${application.topic.orders}")
    @SuppressWarnings("unused")
    public void receive(@Payload Order order) {
        // process the received record accordingly
        LOGGER.info("Received the Order '{}'", order.toString());
    }

    @KafkaListener(topics = "${application.topic.counts}")
    @SuppressWarnings("unused")
    public void receive(@Payload Count count) {
        // process the received record accordingly
        LOGGER.info("Received the Count '{}'", count.toString());
    }
}

