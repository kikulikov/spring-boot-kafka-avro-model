package io.confluent.web;

import io.confluent.datasource.DataSource;
import io.confluent.model.avro.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BasicWebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicWebController.class);

    @Value("${application.topic.orders}")
    private String topicOrders;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private KafkaTemplate<String, Order> kafkaTemplate;

    @PostMapping(path = "/order", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> process() {

        final var order = dataSource.nextOrder();

        LOGGER.info("Sending='{}'", order);
        kafkaTemplate.send(topicOrders, order.getOrderId(), order);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
