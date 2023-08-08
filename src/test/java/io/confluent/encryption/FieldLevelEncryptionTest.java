package io.confluent.encryption;

import io.confluent.datasource.DataSourceFaker;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;
import io.confluent.model.avro.Order;
import io.confluent.testcontainers.SchemaRegistryContainer;
import okhttp3.*;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class FieldLevelEncryptionTest {

    // private static final String KAFKA_IMAGE_NAME = "confluentinc/cp-kafka:7.3.1";
    // private static final DockerImageName KAFKA_IMAGE = DockerImageName.parse(KAFKA_IMAGE_NAME);
    // private static final String SCHEMA_REGISTRY_IMAGE_NAME = "confluentinc/cp-schema-registry:7.3.1";
    // private static final DockerImageName SCHEMA_REGISTRY_IMAGE = DockerImageName.parse(SCHEMA_REGISTRY_IMAGE_NAME);
    //
    // private static final String TOPIC = "ducks";
    // private static final boolean IS_KEY = false;
    // private static final Network network = Network.newNetwork();
    //
    // @Container
    // public static KafkaContainer kafka = new KafkaContainer(KAFKA_IMAGE).withNetwork(network);
    //
    // @Container
    // public static SchemaRegistryContainer schemaRegistry = new SchemaRegistryContainer(SCHEMA_REGISTRY_IMAGE).withKafka(kafka);
    //
    // @BeforeAll
    // public static void before() {
    //     kafka.start();
    //     schemaRegistry.start();
    // }
    //
    // @AfterAll
    // public static void after() {
    //     schemaRegistry.stop();
    //     kafka.stop();
    // }
    //
    // private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    // private final OkHttpClient client = new OkHttpClient();
    //
    // private Response submitSchema(RequestBody body, String url) throws IOException {
    //     final var request = new Request.Builder().header("Content-Type", "application/json").url(url).post(body).build();
    //     try (Response response = client.newCall(request).execute()) {
    //         return response;
    //     }
    // }
    //
    // @Test
    // public void testProducer() throws IOException {
    //
    //     submitSchema(RequestBody.create(metadata, JSON),
    //             schemaRegistry.getURL() + "/subjects/field-metadata/versions");
    //     submitSchema(RequestBody.create(classifications, JSON),
    //             schemaRegistry.getURL() + "/subjects/field-classifications/versions");
    //
    //     final var faker = new DataSourceFaker();
    //     final var order = faker.nextOrder();
    //     final var orderCopy = Order.newBuilder(order).build();
    //     final var recordHeaders = new RecordHeaders();
    //
    //     try (var serializer = new SecuredSpecificAvroSerializer<Order>()) {
    //         serializer.configure(producerConfig(), IS_KEY);
    //
    //         final var encryptedBytes = serializer.serialize(TOPIC, recordHeaders, order);
    //         final var encryptedString = new String(encryptedBytes, StandardCharsets.UTF_8);
    //         printHeaders(recordHeaders);
    //
    //         // TODO assertThat(recordHeaders.toString()).contains("DataCipher");
    //         // TODO assertThat(recordHeaders.toString()).contains("MasterCipher");
    //         assertThat(encryptedString).contains(orderCopy.getOrderId());
    //         assertThat(encryptedString).contains(orderCopy.getBookId());
    //         assertThat(encryptedString).doesNotContain(orderCopy.getCardNumber());
    //
    //         // regular avro deserializer
    //         try (final var avroDeserializer = new SpecificAvroDeserializer<Order>()) {
    //             avroDeserializer.configure(Collections.singletonMap("schema.registry.url", schemaRegistry.getURL()), IS_KEY);
    //             final var deserialized = avroDeserializer.deserialize(TOPIC, encryptedBytes);
    //             assertThat(deserialized.getOrderId()).isEqualTo(orderCopy.getOrderId());
    //             assertThat(deserialized.getBookId()).isEqualTo(orderCopy.getBookId());
    //             assertThat(deserialized.getCardNumber()).isNotEqualTo(orderCopy.getCardNumber());
    //         }
    //
    //         // secured avro deserializer
    //         try (final var avroDeserializer = new SecuredSpecificAvroDeserializer<Order>()) {
    //             avroDeserializer.configure(consumerConfig(), IS_KEY);
    //             final var deserialized = avroDeserializer.deserialize(TOPIC, recordHeaders, encryptedBytes);
    //             assertThat(deserialized.getOrderId()).isEqualTo(orderCopy.getOrderId());
    //             assertThat(deserialized.getBookId()).isEqualTo(orderCopy.getBookId());
    //             assertThat(deserialized.getCardNumber()).isEqualTo(orderCopy.getCardNumber());
    //         }
    //     }
    // }
    //
    // private static void printHeaders(RecordHeaders headers) {
    //     for (var iter = headers.iterator(); iter.hasNext(); ) {
    //         final var header = iter.next();
    //         System.out.println("Header: " + header.key() + ", " + new String(header.value()));
    //     }
    // }
    //
    // private Map<String, Object> producerConfig() {
    //     final var config = new HashMap<String, Object>();
    //     config.put("schema.registry.url", schemaRegistry.getURL());
    //     // Encryption configuration
    //     config.put("encryption.provider.name", "generator");
    //     // config.put("cached.provider.class", "CacheCipherProvider");
    //     // config.put("cached.provider.name", "generator");
    //     config.put("generator.provider.class", "GeneratorCipherProvider");
    //     config.put("generator.provider.name", "local");
    //     config.put("local.provider.class", "LocalCipherProvider");
    //     config.put("local.provider.keys", "RSAWrappingKey");
    //     config.put("local.provider.RSAWrappingKey.key.type", "KeyPair");
    //     config.put("local.provider.RSAWrappingKey.key.private", privateMasterKey);
    //     config.put("local.provider.RSAWrappingKey.key", publicMasterKey);
    //     // Field-level encryption configuration
    //     config.put("encryption.metadata.policy.class", "CatalogPolicy");
    //     config.put("encryption.metadata.name", "field-metadata");
    //     config.put("encryption.classifications.name", "field-classifications");
    //     return config;
    // }
    //
    // private Map<String, Object> consumerConfig() {
    //     final var config = new HashMap<String, Object>();
    //     config.put("schema.registry.url", schemaRegistry.getURL());
    //     // Encryption configuration
    //     config.put("encryption.provider.name", "generator");
    //     // config.put("cached.provider.class", "CacheCipherProvider");
    //     // config.put("cached.provider.name", "generator");
    //     config.put("generator.provider.class", "GeneratorCipherProvider");
    //     config.put("generator.provider.name", "local");
    //     config.put("local.provider.class", "LocalCipherProvider");
    //     config.put("local.provider.keys", "RSAWrappingKey");
    //     config.put("local.provider.RSAWrappingKey.key.type", "KeyPair");
    //     config.put("local.provider.RSAWrappingKey.key.private", privateMasterKey);
    //     config.put("local.provider.RSAWrappingKey.key", publicMasterKey);
    //     // Field-level encryption configuration
    //     config.put("encryption.metadata.policy.class", "CatalogPolicy");
    //     config.put("encryption.metadata.name", "field-metadata");
    //     config.put("encryption.classifications.name", "field-classifications");
    //     return config;
    // }
    //
    // final String metadata = "{\"schema\":\"{\\\"type\\\":\\\"record\\\",\\\"name\\\":\\\"Metadata\\\",\\\"fields\\\":[{\\\"name\\\":\\\"card_number\\\",\\\"type\\\":\\\"string\\\",\\\"classifications\\\":[\\\"PII\\\"]}]}\"}";
    // final String classifications = "{\"schema\":\"{\\\"type\\\":\\\"record\\\",\\\"name\\\":\\\"Classifications\\\",\\\"fields\\\":[],\\\"classifications\\\":{\\\"PII\\\":{\\\"encrypt\\\":{\\\"key\\\":\\\"GeneratedKey\\\",\\\"wrapping.key\\\":\\\"RSAWrappingKey\\\"}}}}\"}";
    //
    // private static final String privateMasterKey = """
    //         -----BEGIN PRIVATE KEY-----
    //         MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC0cJlU2f8Pxkfb
    //         2cMgXz3vFdB5F3kTrtFTyho7VOWo9cB+uRsL/OSmF+kdkrBLUxdl9RvM/OW9i1iL
    //         NcoL0yB7kBq/wNAnWtk7c5ry2k5+oa9kMWp/7bBSnAMuqjET/T/YGvLinMQRI7Nu
    //         gl1hNQ/JKy0d0DWpjtrI3vYT67SjGsg6IKGEJY1mnwDRyl10uf2Q1bMtjTbpBCEV
    //         ECrLGi3C5RLMEHvpvhjFx/JGd0flNXepFUrx8Bn4CkaRrA80RQx1gMg2fykX2V2k
    //         aW6qrd4nkfJaXzA236/AMnwu32AJ7mRGoinP3RHw706UTuah8n66GXUQrFF7B3Ru
    //         lgThhgJNAgMBAAECggEAJXanb0Wfvn+1lcD90W/U5RoLYcjs25hVX+c7UQxMbqsv
    //         n2ABbJjCRHX8caZuMgV7ezDL5+CuNo+C/8xYOeJJAT6nPx7iWPl01vImvrtyxrn9
    //         N3lHGm6YSF6aDotJi1PueIkH2uMflktqSsyIZNFmiKcNQ/5h1eWSKViZkQNESwS/
    //         yfJFxqHPqD7zAB007ABRobg8qNMeJCtlzqZ4cIMljeY1uJSmp/jfZjc5TKvyewkT
    //         tclxIAVfA6rxKxoX6REUF5GwAF5eqkSy/0DVazrAdpfhhyVceQwz6aNQAJ6kl82Q
    //         +2v+4JrF9mfmLLDIe5134g8ce0USTFo2HodOX0z4oQKBgQDuWPB2lTRKQJSlOeLA
    //         Ge8NulnN+0S6pLp+P6S+emP4S06BuucfoQzW1lYUQlMcasDUDMQXR1sEhFEBCgeP
    //         sPkP46FFuKxvE+VAV8PGfmdnSkKn0GGBo1grH7wL6zEa8Aaki/CaVccDiAUn3s22
    //         doueo16rldi8s3bU8bQuPP1ddQKBgQDBzbvFjB7+SfHWtcIn1uczwU1S6uhY6P4i
    //         9H6v0dbGPSNR+CallvxWLWfnsD7mxT+9Rnd9Wfl5S+B2w83oOkyfj5ApN/9778Js
    //         DhggZGmdRp+HqioqQDV9jkjD6XmbIOapapDbOp1kdgL+TfkMQ+X9ur+0wjIg/b09
    //         leC2I8i+eQKBgDbhP6zuOfYG3LMWmwFEd2ifyeHFw1N+bnp3SQWaxL39CPyR/nmn
    //         6X7mJzfO9bgLVI7+yX/arDhkBwrINml9hDuGXDHjjcCdwiGIk8l2fXSpAqQFNQ5W
    //         gAyd5/yOkOX08nKczi+bJJHJlfZSNOeYcBl84GV9wUPhxrDNXoFvBZ+pAoGAQqqS
    //         vRHGV5L4LfO4Jhw2zAbionNMGcmMIloYekKkruy5CaljIfMeOkbER17JRHj1xBZJ
    //         ZEVfG+qN/Ey+t/PWB81KueMZb3i6WJFAm2PvMLrqhbBzcLVFsTsQrNPTkRlwPzb5
    //         PXCcU8KEpRYNZbq1kuJ6r6NduxuNzXGRKxaEJ4ECgYBiLPbtH0apB102NFYHDrH6
    //         9PjlAujXq57VPbZhqtt2w+IU+Mrk2w8SqfbiEEMXUlFJ/l+IBGVWHAC/euCWPde8
    //         zLxGQvRcXx0W/6YuWsu2o9cbAsMqsTPwjRXsnCEaDuMfRnqtFLBJTjdVtHknHcEG
    //         +M7pGAYKWiA0icliEUb+lA==
    //         -----END PRIVATE KEY-----
    //         """;
    //
    // private static final String publicMasterKey = """
    //         -----BEGIN PUBLIC KEY-----
    //         MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtHCZVNn/D8ZH29nDIF89
    //         7xXQeRd5E67RU8oaO1TlqPXAfrkbC/zkphfpHZKwS1MXZfUbzPzlvYtYizXKC9Mg
    //         e5Aav8DQJ1rZO3Oa8tpOfqGvZDFqf+2wUpwDLqoxE/0/2Bry4pzEESOzboJdYTUP
    //         ySstHdA1qY7ayN72E+u0oxrIOiChhCWNZp8A0cpddLn9kNWzLY026QQhFRAqyxot
    //         wuUSzBB76b4YxcfyRndH5TV3qRVK8fAZ+ApGkawPNEUMdYDINn8pF9ldpGluqq3e
    //         J5HyWl8wNt+vwDJ8Lt9gCe5kRqIpz90R8O9OlE7mofJ+uhl1EKxRewd0bpYE4YYC
    //         TQIDAQAB
    //         -----END PUBLIC KEY-----
    //         """;
}
