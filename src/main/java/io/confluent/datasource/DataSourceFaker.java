package io.confluent.datasource;

import io.confluent.model.avro.Book;
import io.confluent.model.avro.Order;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DataSourceFaker implements DataSource {

  private static final Faker FAKER = new Faker();

  @Override
  public Book nextBook() {
    final var title = FAKER.harryPotter().book();
    return new Book(UUID.nameUUIDFromBytes(title.getBytes()).toString(), title);
  }

  @Override
  public Order nextOrder() {
    return new Order(FAKER.internet().uuid(), nextBook().getBookId(),
        FAKER.random().nextInt(1, 10), FAKER.finance().creditCard());
  }
}