package io.confluent.datasource;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DataSourceFakerTest {

    public static final Logger LOGGER = LoggerFactory.getLogger(DataSourceFakerTest.class);
    public static final DataSource eventSource = new DataSourceFaker();

    @Test
    void generateBook() {
        final var book = eventSource.nextBook();
        LOGGER.info(book.toString());
        assertNotNull(book.getBookId());
        assertNotNull(book.getBookTitle());
    }

    @Test
    void generateOrder() {
        final var order = eventSource.nextOrder();
        LOGGER.info(order.toString());
        assertNotNull(order.getOrderId());
        assertNotNull(order.getBookId());
        assertNotNull(order.getCardNumber());
        assertNotEquals(0, order.getQuantity());
    }
}