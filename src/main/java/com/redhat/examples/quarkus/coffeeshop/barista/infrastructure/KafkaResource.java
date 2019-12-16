package com.redhat.examples.quarkus.coffeeshop.barista.infrastructure;

import com.redhat.examples.quarkus.Barista;
import com.redhat.examples.quarkus.coffeeshop.barista.domain.BeverageOrder;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class KafkaResource {

    @Inject
    Barista barista;

    private Jsonb jsonb = JsonbBuilder.create();

    @Incoming("orders")
    @Outgoing("orders")
    public CompletionStage<BeverageOrder> prepare(String message) {

        BeverageOrder beverageOrder = jsonb.fromJson(message, BeverageOrder.class);
        return barista.orderIn(beverageOrder);
    }


}
