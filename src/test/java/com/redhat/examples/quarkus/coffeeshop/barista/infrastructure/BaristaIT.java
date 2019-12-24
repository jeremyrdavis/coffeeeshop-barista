package com.redhat.examples.quarkus.coffeeshop.barista.infrastructure;

import com.redhat.examples.quarkus.coffeeshop.barista.domain.Beverage;
import com.redhat.examples.quarkus.coffeeshop.barista.domain.BeverageOrder;
import com.redhat.examples.quarkus.coffeeshop.barista.domain.Status;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest @Testcontainers
public class BaristaIT extends BaseTestContainersIT {


    @Test
    public void testBlackCoffeeOrderInFromKafka() throws ExecutionException, InterruptedException {

        BeverageOrder beverageOrder = new BeverageOrder(UUID.randomUUID().toString(), "Jeremy", Beverage.BLACK_COFFEE);
        kafkaProducer.send(new ProducerRecord<>(INCOMING_TOPIC_NAME, beverageOrder.orderId, jsonb.toJson(beverageOrder).toString())).get();
//        ConsumerRecords<String, String> initialRecords = kafkaConsumer.poll(Duration.ofMillis(10000));
//        assertEquals(1, initialRecords.count());

        //Give the Barista time to make the drink
        Thread.sleep(5010);

        DescribeClusterResult describeClusterResult = kafkaAdminClient.describeCluster();

        ConsumerRecords<String, String> newRecords = kafkaConsumer.poll(Duration.ofMillis(10000));
        assertEquals(1, newRecords.count());
        for (ConsumerRecord<String, String> record : newRecords) {
            System.out.printf("offset = %d, key = %s, value = %s\n",
                    record.offset(),
                    record.key(),
                    record.value());
//            assertEquals(beverageOrder.orderId, record.key());
//            assertEquals(beverageOrder.toString(), record.value());
            System.out.println(record.value());
            BeverageOrder result = jsonb.fromJson(record.value(), BeverageOrder.class);
            assertEquals(Status.READY, result.status);
        }
    }
}

