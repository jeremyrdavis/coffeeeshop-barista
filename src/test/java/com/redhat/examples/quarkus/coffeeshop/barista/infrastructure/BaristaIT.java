package com.redhat.examples.quarkus.coffeeshop.barista.infrastructure;

import static io.restassured.RestAssured.given;
import static java.util.Collections.singletonList;

import com.redhat.examples.quarkus.Barista;
import com.redhat.examples.quarkus.coffeeshop.barista.domain.Beverage;
import com.redhat.examples.quarkus.coffeeshop.barista.domain.BeverageOrder;
import io.quarkus.test.junit.QuarkusTest;
import javafx.scene.media.MediaPlayer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest @Testcontainers
public class BaristaIT {

    Jsonb jsonb = JsonbBuilder.create();

    //Kafka stuff
    final String TOPIC_NAME = "orders";

    static DockerComposeContainer dockerComposeContainer;

    KafkaProducer<String, String> kafkaProducer;

    KafkaConsumer<String, String> kafkaConsumer;

    AdminClient kafkaAdminClient;


    @BeforeAll
    public static void setUpAll() {
        dockerComposeContainer = new DockerComposeContainer(
                new File("src/test/resources/docker-compose.yaml"))
                .withExposedService("kafka", 9092)
                .withExposedService("zookeeper", 2181);
        dockerComposeContainer.start();
    }

    @BeforeEach
    public void setUp() {

        setUpAdminClient();
        setUpProducer();
        setUpTopics();
        setUpConsumer();
    }

    @AfterEach
    public void tearDown() {
    }

    @AfterAll
    public static void tearDownAll() {
        dockerComposeContainer.stop();
    }

    void setUpAdminClient() {

        Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", "localhost:9092");
        kafkaAdminClient = AdminClient.create(config);
    }

    void setUpTopics() {
        //create Topics

        List<NewTopic> topics = new ArrayList<>();
        topics.add(new NewTopic(TOPIC_NAME, 4, (short) 1));

        kafkaAdminClient.createTopics(topics);
        kafkaAdminClient.close();
    }

    void setUpProducer(){
        //create Producer config
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put("input.topic.name", TOPIC_NAME);

        //initialize the Producer
        kafkaProducer = new KafkaProducer(
                props,
                new StringSerializer(),
                new StringSerializer()
        );
    }

    void setUpConsumer() {

        //create Consumer config
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "testgroup" + new Random().nextInt());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        //initialize the Consumer
        kafkaConsumer = new KafkaConsumer(props);
        //subscribe
        kafkaConsumer.subscribe(singletonList(TOPIC_NAME));
    }

    @Test
    public void testBlackCoffeeOrderInFromKafka() throws ExecutionException, InterruptedException {

        given()
                .when().get("/api")
                .then()
                .statusCode(200)
                .body(is("hello"));

        BeverageOrder beverageOrder = new BeverageOrder(UUID.randomUUID().toString(), "Jeremy", Beverage.BLACK_COFFEE);
        kafkaProducer.send(new ProducerRecord<>(TOPIC_NAME, beverageOrder.orderId, beverageOrder.toString())).get();

        //Give the Barista time to make the drink
        Thread.sleep(5010);

        List<PartitionInfo> partitionInfos = kafkaConsumer.partitionsFor(TOPIC_NAME);

        ConsumerRecords<String, String> allRecords = kafkaConsumer.poll(Duration.ofMillis(10000));
        assertFalse(allRecords.isEmpty());
        for (ConsumerRecord<String, String> record : allRecords) {
            System.out.printf("offset = %d, key = %s, value = %s\n",
                    record.offset(),
                    record.key(),
                    record.value());
            assertEquals(beverageOrder.orderId, record.key());
            assertEquals(beverageOrder.toString(), record.value());
            System.out.println(record.value());
            BeverageOrder result = jsonb.fromJson(record.value(), BeverageOrder.class);
            assertEquals(MediaPlayer.Status.READY, result.status);
        }
    }
}

