package io.confluent.datasource;

import io.confluent.model.avro.Book;
import io.confluent.model.avro.Order;

public interface DataSource {

    Book nextBook();

    Order nextOrder();
}