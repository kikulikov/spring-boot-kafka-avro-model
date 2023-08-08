package io.confluent.encryption;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.vault.VaultContainer;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class KafkaStreamsEncryptionTest {

    // private static final String INPUT_TOPIC = "input";
    // private static final String OUTPUT_TOPIC = "output";
    // private static final String KAFKA_IMAGE_NAME = "confluentinc/cp-kafka:7.3.1";
    // private static final DockerImageName KAFKA_IMAGE = DockerImageName.parse(KAFKA_IMAGE_NAME);
    // private static final Serde<String> STRING_SERDE = Serdes.String();
    //
    // private KafkaProducer<String, String> kafkaProducer;
    //
    // @Container
    // @ClassRule
    // public final VaultContainer<?> vaultContainer = new VaultContainer<>("vault:latest")
    //         .withSecretInVault("secret/master-public",
    //                 "WrappingMasterKey=" + URLEncoder.encode(publicMasterKey, StandardCharsets.US_ASCII))
    //         .withSecretInVault("secret/master-private",
    //                 "WrappingMasterKey=" + URLEncoder.encode(privateMasterKey, StandardCharsets.US_ASCII))
    //         .withVaultToken(VAULT_TOKEN);
    //
    // @Container
    // @ClassRule
    // public static final KafkaContainer kafka = new KafkaContainer(KAFKA_IMAGE);
    //
    // private KafkaStreams kafkaStreams;
    //
    // @BeforeEach
    // void setUp() {
    //     kafka.withEnv("KAFKA_LOG_FLUSH_INTERVAL_MESSAGES", "1");
    //
    //     Startables.deepStart(kafka);
    //
    //     Unreliables.retryUntilTrue(60, TimeUnit.SECONDS,
    //             () -> kafka.isRunning() && !kafka.getBootstrapServers().isBlank());
    //
    //     createTopics(INPUT_TOPIC, OUTPUT_TOPIC);
    //
    //     kafkaStreams = new KafkaStreams(simpleTopology(), streamsProperties());
    //     kafkaStreams.start();
    //
    //     Unreliables.retryUntilTrue(60, TimeUnit.SECONDS,
    //             () -> kafkaStreams.state().equals(KafkaStreams.State.RUNNING));
    //
    //     this.kafkaProducer = new KafkaProducer<>(producerConfig());
    // }
    //
    // private void createTopics(String... topics) {
    //     final var newTopics =
    //             Arrays.stream(topics)
    //                     .map(topic -> new NewTopic(topic, 1, (short) 1))
    //                     .collect(Collectors.toList());
    //
    //     final var config = Map.<String, Object>of(BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
    //
    //     try (var admin = AdminClient.create(config)) {
    //         admin.createTopics(newTopics);
    //     }
    // }
    //
    // private Topology simpleTopology() {
    //     final var streamsBuilder = new StreamsBuilder();
    //
    //     final var securedStringSerializer = new SecuredStringSerializer();
    //     securedStringSerializer.configure(producerConfig(), false);
    //
    //     final var securedStringDeserializer = new SecuredStringDeserializer();
    //     securedStringDeserializer.configure(consumerConfig(), false);
    //
    //     final var securedSerde = new SecuredStringSerde(securedStringSerializer, securedStringDeserializer);
    //
    //     final KStream<String, String> onlineOrders = streamsBuilder
    //             .stream(INPUT_TOPIC, Consumed.with(STRING_SERDE, securedSerde));
    //
    //     onlineOrders.mapValues(s -> s.toUpperCase())
    //             .to(OUTPUT_TOPIC, Produced.with(STRING_SERDE, securedSerde));
    //
    //     return streamsBuilder.build();
    // }
    //
    // @AfterEach
    // void tearDown() {
    //     vaultContainer.stop();
    //
    //     if (kafkaStreams != null) {
    //         kafkaStreams.close();
    //     }
    //     kafka.close();
    // }
    //
    // private RecordMetadata produce(final String topic, final String value) throws Exception {
    //     return kafkaProducer.send(new ProducerRecord<>(topic, UUID.randomUUID().toString(), value)).get();
    // }
    //
    // private ConsumerRecords<String, String> consume(final String topic, Map<String, Object> config) {
    //     try (var consumer = new KafkaConsumer<String, String>(config)) {
    //         consumer.subscribe(Collections.singletonList(topic));
    //         for (int i = 0; i < 10; i++) {
    //             final var records = consumer.poll(Duration.ofMillis(100));
    //             if (!records.isEmpty()) {
    //                 return records;
    //             }
    //         }
    //         return new ConsumerRecords<>(Collections.emptyMap());
    //     }
    // }
    //
    // @Test
    // void testKafkaStreamsProcessing() throws Exception {
    //     final var produced = produce(INPUT_TOPIC, "quack");
    //     assertThat(produced.offset()).isEqualTo(0);
    //
    //     final var plaintextInput = consume(INPUT_TOPIC, plaintextConsumerConfig());
    //     final var nextPlaintextInput = plaintextInput.iterator().next();
    //     assertThat(nextPlaintextInput.value()).isNotEqualToIgnoringCase("quack");
    //
    //     final var decryptedInput = consume(INPUT_TOPIC, consumerConfig());
    //     final var nextDecryptedInput = decryptedInput.iterator().next();
    //     assertThat(nextDecryptedInput.value()).isEqualTo("quack");
    //
    //     final var plaintextOutput = consume(OUTPUT_TOPIC, plaintextConsumerConfig());
    //     final var nextPlaintextOutput = plaintextOutput.iterator().next();
    //     assertThat(nextPlaintextOutput.value()).isNotEqualToIgnoringCase("quack");
    //
    //     final var decryptedOutput = consume(OUTPUT_TOPIC, consumerConfig());
    //     final var nextDecryptedOutput = decryptedOutput.iterator().next();
    //     assertThat(nextDecryptedOutput.value()).isEqualTo("QUACK");
    // }
    //
    // private Properties streamsProperties() {
    //     final var props = new Properties();
    //     props.putAll(streamsConfig());
    //     return props;
    // }
    //
    // private Map<String, String> streamsConfig() {
    //     final var config = new HashMap<String, String>();
    //     config.put("application.id", "encrypted-streams");
    //     config.put("bootstrap.servers", kafka.getBootstrapServers());
    //     return config;
    // }
    //
    // private Map<String, Object> producerConfig() {
    //     final var config = new HashMap<String, Object>();
    //     config.put("client.id", "encrypted-producer");
    //     config.put("bootstrap.servers", kafka.getBootstrapServers());
    //     config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    //     config.put("value.serializer", "io.confluent.encryption.serializers.common.SecuredStringSerializer");
    //     // encryption
    //     config.put("value.serializer.key", "GeneratedDataCipher");
    //     config.put("value.serializer.key.provider.name", "cached");
    //     config.put("value.serializer.wrapping.key", "WrappingMasterKey");
    //     config.put("value.serializer.wrapping.key.provider.name", "cachedvault");
    //     // default cipher
    //     config.put("encryption.provider.name", "vault");
    //     // data cipher
    //     config.put("cached.provider.class", "CacheCipherProvider");
    //     config.put("cached.provider.name", "generator");
    //     config.put("generator.provider.class", "GeneratorCipherProvider");
    //     config.put("generator.provider.name", "local");
    //     config.put("local.provider.class", "LocalCipherProvider");
    //     // master cipher
    //     config.put("cachedvault.provider.class", "CacheCipherProvider");
    //     config.put("cachedvault.provider.expiry", "600"); // seconds
    //     config.put("cachedvault.provider.name", "vault");
    //     config.put("vault.provider.class", "io.confluent.encryption.common.crypto.impl.VaultSecretCipherProvider");
    //     config.put("vault.provider.url", "http://" + vaultContainer.getHost() + ":" + vaultContainer.getMappedPort(8200));
    //     config.put("vault.provider.token", VAULT_TOKEN);
    //     // public key from vault
    //     config.put("vault.provider.path", "secret/data/master-public");
    //     config.put("vault.provider.keys", "WrappingMasterKey");
    //     config.put("vault.provider.WrappingMasterKey.key.type", "PublicKey");
    //     // symmetric key from local
    //     // config.put("vault.provider.class", "LocalCipherProvider");
    //     // config.put("vault.provider.keys", "WrappingMasterKey");
    //     // config.put("vault.provider.WrappingMasterKey.key.type", "SymmetricKey");
    //     // config.put("vault.provider.WrappingMasterKey.key", symmetricDataKey);
    //     // private & public key pair from local
    //     // config.put("vault.provider.WrappingMasterKey.key", publicMasterKey);
    //     // config.put("vault.provider.WrappingMasterKey.key.type", "KeyPair");
    //     // config.put("vault.provider.WrappingMasterKey.key.private", privateMasterKey);
    //     // config.put("vault.provider.WrappingMasterKey.key", publicMasterKey);
    //     return config;
    // }
    //
    // private Map<String, Object> plaintextConsumerConfig() {
    //     final var config = new HashMap<String, Object>();
    //     config.put("group.id", "plaintext-consumer");
    //     config.put("auto.offset.reset", "earliest");
    //     config.put("bootstrap.servers", kafka.getBootstrapServers());
    //     config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    //     config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    //     return config;
    // }
    //
    // private Map<String, Object> consumerConfig() {
    //     final var config = new HashMap<String, Object>();
    //     config.put("group.id", "encrypted-consumer");
    //     config.put("auto.offset.reset", "earliest");
    //     config.put("bootstrap.servers", kafka.getBootstrapServers());
    //     config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    //     config.put("value.deserializer", "io.confluent.encryption.serializers.common.SecuredStringDeserializer");
    //     // encryption
    //     config.put("value.deserializer.key", "GeneratedDataCipher");
    //     config.put("value.deserializer.key.provider.name", "cached");
    //     config.put("value.deserializer.wrapping.key", "WrappingMasterKey");
    //     config.put("value.deserializer.wrapping.key.provider.name", "cachedvault");
    //     // default cipher
    //     config.put("encryption.provider.name", "vault");
    //     // data cipher
    //     config.put("cached.provider.class", "CacheCipherProvider");
    //     config.put("cached.provider.name", "generator");
    //     config.put("generator.provider.class", "GeneratorCipherProvider");
    //     config.put("generator.provider.name", "local");
    //     config.put("local.provider.class", "LocalCipherProvider");
    //     // master cipher
    //     config.put("cachedvault.provider.class", "CacheCipherProvider");
    //     config.put("cachedvault.provider.expiry", "600"); // seconds
    //     config.put("cachedvault.provider.name", "vault");
    //     config.put("vault.provider.class", "io.confluent.encryption.common.crypto.impl.VaultSecretCipherProvider");
    //     config.put("vault.provider.url", "http://" + vaultContainer.getHost() + ":" + vaultContainer.getMappedPort(8200));
    //     config.put("vault.provider.token", VAULT_TOKEN);
    //     // public key from vault
    //     config.put("vault.provider.path", "secret/data/master-private");
    //     config.put("vault.provider.keys", "WrappingMasterKey");
    //     config.put("vault.provider.WrappingMasterKey.key.type", "PrivateKey");
    //     // symmetric key from local
    //     // config.put("vault.provider.class", "LocalCipherProvider");
    //     // config.put("vault.provider.keys", "WrappingMasterKey");
    //     // config.put("vault.provider.WrappingMasterKey.key.type", "SymmetricKey");
    //     // config.put("vault.provider.WrappingMasterKey.key", symmetricDataKey);
    //     // private & public key pair from local
    //     // config.put("vault.provider.WrappingMasterKey.key", privateMasterKey);
    //     // config.put("vault.provider.WrappingMasterKey.key.type", "KeyPair");
    //     // config.put("vault.provider.WrappingMasterKey.key.private", privateMasterKey);
    //     // config.put("vault.provider.WrappingMasterKey.key", publicMasterKey);
    //     return config;
    // }
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
    //
    // private static final String VAULT_TOKEN = "confluent";
}
