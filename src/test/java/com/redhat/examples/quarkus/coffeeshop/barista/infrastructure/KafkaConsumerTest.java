package com.redhat.examples.quarkus.coffeeshop.barista.infrastructure;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class KafkaConsumerTest {


    @Test
    public void testConsumer() {

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(consumerProps);

        kafkaConsumer.subscribe(Arrays.asList("orders"));

        ConsumerRecords<String, String> records;
        final int giveUp = 10;
        int noRecordsCount = 0;
        while(true){
            records = kafkaConsumer.poll(Duration.ofMillis(5000));
            if (records.count()==0) {
                System.out.println("No records found");
                noRecordsCount++;
                if (noRecordsCount > giveUp) break;
                else continue;
            }
        }
        assertFalse(records.isEmpty());
    }
}
