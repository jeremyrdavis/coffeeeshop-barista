package com.redhat.examples.quarkus.coffeeshop.barista.infrastructure;

import com.redhat.examples.quarkus.Barista;
import com.redhat.examples.quarkus.coffeeshop.barista.domain.BeverageOrder;
import com.redhat.examples.quarkus.coffeeshop.barista.domain.Status;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class KafkaResource {

    Logger logger = Logger.getLogger(KafkaResource.class);

    @Inject
    Barista barista;

    private Jsonb jsonb = JsonbBuilder.create();

    @Incoming("coffee-orders-in")
    @Outgoing("coffee-orders-out")
    public CompletionStage<BeverageOrder> orderIn(String message) {

        logger.debug(message);

        BeverageOrder beverageOrder = jsonb.fromJson(message, BeverageOrder.class);
        logger.debug(beverageOrder.toString());
        return barista.orderIn(beverageOrder);
    }

    public class KafkaTopics{

        public static final String INCOMING = "barista-orders-in";
        public static final String OUTGOING = "barista-orders-up";
    }


}
