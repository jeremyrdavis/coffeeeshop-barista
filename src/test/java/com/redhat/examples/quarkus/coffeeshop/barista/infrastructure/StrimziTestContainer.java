package com.redhat.examples.quarkus.coffeeshop.barista.infrastructure;

import org.testcontainers.containers.GenericContainer;

public class StrimziTestContainer extends GenericContainer<StrimziTestContainer> {

    public static final int KAFKA_PORT = 9092;
    public static final String DEFAULT_IMAGE_AND_TAG = "strimzi/kafka:0.11.3-kafka-2.1.0";

    public StrimziTestContainer() {
        this(DEFAULT_IMAGE_AND_TAG);
    }

    public StrimziTestContainer(String image) {
        super(image);
        addExposedPort(KAFKA_PORT);
    }

    public Integer getPort() {
        return getMappedPort(KAFKA_PORT);
    }
}
