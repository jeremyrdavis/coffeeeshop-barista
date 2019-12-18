package com.redhat.examples.quarkus.coffeeshop.barista.infrastructure;

import com.redhat.examples.quarkus.coffeeshop.barista.domain.Beverage;
import com.redhat.examples.quarkus.coffeeshop.barista.domain.BeverageOrder;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTest
@Testcontainers
public class BaristaIT {

    static final String TOPIC_NAME = "orders";

    KafkaProducer<String, String> kafkaProducer;

    KafkaConsumer<String, String> kafkaConsumer;

/*
    @Container
    GenericContainer zookeeper = new GenericContainer<>("strimzi/kafka:0.11.3-kafka-2.1.0")
            .withEnv("LOG_DIR", "/tmp/logs")
            .withExposedPorts(2181)
            .withCommand("sh", "-c", "bin/zookeeper-server-start.sh config/zookeeper.properties");

    @Container
    GenericContainer kafka = new GenericContainer<>("strimzi/kafka:0.11.3-kafka-2.1.0")
            .withEnv("LOG_DIR", "/tmp/logs")
            .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:9092")
            .withEnv("KAFKA_LISTENERS", "PLAINTEXT://0.0.0.0:9092")
            .withEnv("KAFKA_ZOOKEEPER_CONNECT", "zookeeper:2181")
            .withExposedPorts(9092)
            .withCommand("sh", "-c", "bin/kafka-server-start.sh config/server.properties --override listeners=$${KAFKA_LISTENERS} --override advertised.listeners=$${KAFKA_ADVERTISED_LISTENERS} --override zookeeper.connect=$${KAFKA_ZOOKEEPER_CONNECT}");
*/

    public KafkaContainer kafka = new KafkaContainer()
            .withEnv("LOG_DIR", "/tmp/logs")
            .withEnv("KAKFKA_ZOOKEEPER_CONNECT", "localhost:32181")
            .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:29092")
            .withEnv("KAFKA_LISTENERS", "PLAINTEXT://0.0.0.0:29092")
            .withExposedPorts(32800, 32801, 32802, 32804,32796,32798,9093)
            .withCommand("sh", "-c", "bin/kafka-server-start.sh config/server.properties --override listeners=$${KAFKA_LISTENERS} --override advertised.listeners=$${KAFKA_ADVERTISED_LISTENERS}");

    @BeforeEach
    public void setUp() {

        //start Kafka
        kafka.start();

        //create Producer config
        String bootstrapServers = kafka.getBootstrapServers();
        Map<String, String> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());

        //initialize the Producer
        kafkaProducer = new KafkaProducer(
                config,
                new StringSerializer(),
                new StringSerializer()
        );

        //create Consumer config
        Map<String, String> consumerConfig = new HashMap<>();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "tc-" + UUID.randomUUID());
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        //initialize the Consumer
        kafkaConsumer = new KafkaConsumer(
                consumerConfig,
                new StringDeserializer(),
                new StringDeserializer()
        );
    }

    @AfterEach
    public void tearDown() {
        kafka.stop();
    }

    @Test
    public void testOrderInFromKafka() throws ExecutionException, InterruptedException {

        BeverageOrder beverageOrder = new BeverageOrder("Jeremy", Beverage.BLACK_COFFEE);
        kafkaProducer.send(new ProducerRecord<>(TOPIC_NAME, "testcontainers", "rulezzz")).get();

        Thread.sleep(5010);

        ConsumerRecords<String, String> records = kafkaConsumer.poll(100);
        assertFalse(records.isEmpty());
        fail("unimplemented");
    }
}

