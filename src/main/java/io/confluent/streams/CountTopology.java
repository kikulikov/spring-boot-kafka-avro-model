package io.confluent.streams;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.confluent.model.avro.Book;
import io.confluent.model.avro.Count;
import io.confluent.model.avro.Order;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.*;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafkaStreams
public class CountTopology {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountTopology.class);

    @Value("${application.topic.books}")
    private String topicBooks;

    @Value("${application.topic.orders}")
    private String topicOrders;

    @Value("${application.topic.counts}")
    private String topicCounts;

    private static final Serde<String> STRING_SERDE = Serdes.String();
    private static final Serde<Integer> INTEGER_SERDE = Serdes.Integer();
    private static final Serde<Book> booksSerde = new SpecificAvroSerde<>();
    private static final Serde<Order> ordersSerde = new SpecificAvroSerde<>();
    private static final Serde<Count> countsSerde = new SpecificAvroSerde<>();

    @Bean
    public KStream<String, Count> count(StreamsBuilder streamsBuilder, Environment env) {

        booksSerde.configure(getAllProperties(env), false);
        ordersSerde.configure(getAllProperties(env), false);
        countsSerde.configure(getAllProperties(env), false);

        final MutablePropertySources sources = ((AbstractEnvironment) env).getPropertySources();

        final Materialized<String, Book, KeyValueStore<Bytes, byte[]>> bookstore =
                Materialized.<String, Book>as(Stores.persistentKeyValueStore("bookstore"))
                        .withKeySerde(STRING_SERDE).withValueSerde(booksSerde).withCachingEnabled();

        final KTable<String, Book> booksTable = streamsBuilder
                .table(topicBooks, Consumed.with(STRING_SERDE, booksSerde), bookstore);

        final KStream<String, Order> ordersStream = streamsBuilder
                .stream(topicOrders, Consumed.with(STRING_SERDE, ordersSerde));

        final KTable<String, Count> countsTable = ordersStream.groupBy((m, n) -> n.getBookId())
                .aggregate(() -> 0, (bookId, order, total) -> order.getQuantity() + total, Materialized.with(STRING_SERDE, INTEGER_SERDE))
                .join(booksTable, (total, book) -> new Count(book.getBookId(), book.getBookTitle(), total));

        final KStream<String, Count> countsStream = countsTable.toStream();

        countsStream.peek((m, n) -> LOGGER.info("Counted: '{}'", n));
        countsStream.to(topicCounts, Produced.with(STRING_SERDE, countsSerde));

        return countsStream;
    }

    private Map<String, Object> getAllProperties(Environment env) {
        final var result = new HashMap<String, Object>();

        if (env instanceof ConfigurableEnvironment) {
            for (PropertySource<?> propertySource : ((ConfigurableEnvironment) env).getPropertySources()) {
                if (propertySource instanceof EnumerablePropertySource) {
                    for (String key : ((EnumerablePropertySource<?>) propertySource).getPropertyNames()) {
                        if (key.startsWith("spring.kafka.properties")) {
                            result.put(key.replaceFirst("spring.kafka.properties.", ""), propertySource.getProperty(key));
                        }
                        if (key.startsWith("spring.kafka.producer.properties.")) {
                            result.put(key.replaceFirst("spring.kafka.producer.properties.", ""), propertySource.getProperty(key));
                        }
                        if (key.startsWith("spring.kafka.consumer.properties.")) {
                            result.put(key.replaceFirst("spring.kafka.consumer.properties.", ""), propertySource.getProperty(key));
                        }
                    }
                }
            }
        }
        return result;
    }
}