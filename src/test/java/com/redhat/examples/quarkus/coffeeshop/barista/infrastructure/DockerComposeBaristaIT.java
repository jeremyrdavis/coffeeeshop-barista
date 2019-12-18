package com.redhat.examples.quarkus.coffeeshop.barista.infrastructure;

import com.redhat.examples.quarkus.coffeeshop.barista.domain.Beverage;
import com.redhat.examples.quarkus.coffeeshop.barista.domain.BeverageOrder;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.json.bind.Jsonb;
import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@Testcontainers
public class DockerComposeBaristaIT {

    static final String TOPIC_NAME = "orders";

    DockerComposeContainer dockerComposeContainer;

    KafkaProducer<String, String> kafkaProducer;
    KafkaConsumer<String, String> kafkaConsumer;

    @BeforeEach
    public void setUp() {
        dockerComposeContainer = new DockerComposeContainer(
                new File("src/test/resources/docker-compose.yaml"))
                .withExposedService("kafka", 9092)
                .withExposedService("zookeeper", 2181);

        dockerComposeContainer.start();

        //create Producer config
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put("input.topic.name", TOPIC_NAME);
        props.put("input.topic.partitions", "4");
        props.put("input.topic.replication.factor", "1");

        //initialize the Producer
        kafkaProducer = new KafkaProducer(
                props,
                new StringSerializer(),
                new StringSerializer()
        );

        //create Topics
        Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", "localhost:9092");
        AdminClient client = AdminClient.create(config);

        List<NewTopic> topics = new ArrayList<>();
        topics.add(new NewTopic(TOPIC_NAME, 4, (short) 1));

        client.createTopics(topics);
        client.close();

        //create Consumer config
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "testgroup" + new Random().nextInt());
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        consumerProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        //initialize the Consumer
        kafkaConsumer = new KafkaConsumer(
                consumerProps,
                new StringDeserializer(),
                new StringDeserializer()
        );

        kafkaConsumer.subscribe(Arrays.asList(TOPIC_NAME));
    }

    @AfterEach
    public void tearDown() {
        dockerComposeContainer.stop();
    }

    @Test
    public void testSomething() throws InterruptedException, ExecutionException {

/*
        BeverageOrder beverageOrder = new BeverageOrder("Jeremy", Beverage.BLACK_COFFEE);
        RecordMetadata recordMetadata = kafkaProducer.send(new ProducerRecord<>(TOPIC_NAME, "order", beverageOrder.toString())).get();
*/

        long numberOfEvents = 5;
        for (int i = 0; i < numberOfEvents; i++) {
            String key = "testContainers";
            String value = "AreAwesome";
            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, key, value);
            System.out.println("ProducerRecord:" + record.toString());
            try {
                RecordMetadata recordMetadata = kafkaProducer.send(record).get();
                assertNotNull(recordMetadata);
                System.out.println("Offset:" + recordMetadata.offset());
                System.out.println("RecordMetadata:" + recordMetadata.toString());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        kafkaProducer.close();

/*
        Awaitility.await()
                .dontCatchUncaughtExceptions()
                .atLeast(6, TimeUnit.SECONDS)
                .pollInterval(10, TimeUnit.MILLISECONDS)
                .until(future::isDone);
*/
//        Thread.sleep(6000);

        ConsumerRecords<String, String> records;

        final int giveUp = 10;
        int noRecordsCount = 0;

        while(true){

            records = kafkaConsumer.poll(10000);

            if (records.count()==0) {
                System.out.println("No records found");
                noRecordsCount++;
                if (noRecordsCount > giveUp) break;
                else continue;
            }
        }

        assertFalse(records.isEmpty());

        assertEquals(records.count(), 1);
        records.forEach(stringStringConsumerRecord -> {
            assertEquals(stringStringConsumerRecord.key(), "testContainers");
            assertEquals(stringStringConsumerRecord.value(), "are awesome");
        });
    }
}
