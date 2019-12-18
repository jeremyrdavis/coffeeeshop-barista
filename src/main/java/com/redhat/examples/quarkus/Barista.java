package com.redhat.examples.quarkus;

import com.redhat.examples.quarkus.coffeeshop.barista.domain.BeverageOrder;
import com.redhat.examples.quarkus.coffeeshop.barista.domain.Status;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class Barista {

    Logger logger = Logger.getLogger(Barista.class);

    public CompletionStage<BeverageOrder> orderIn(BeverageOrder beverageOrder) {

        logger.debug("orderIn: " + beverageOrder.toString());

        return CompletableFuture.supplyAsync(() -> {

            switch(beverageOrder.beverage){
                case BLACK_COFFEE:
                    return prepare(beverageOrder, 5);
                case COFFEE_WITH_ROOM:
                    return prepare(beverageOrder, 5);
                case LATTE:
                    return prepare(beverageOrder, 7);
                case CAPPUCCINO:
                    return prepare(beverageOrder, 9);
                default:
                    return prepare(beverageOrder, 11);
            }
        });

    }

    private BeverageOrder prepare(final BeverageOrder beverageOrder, int seconds) {
        try {
            Thread.sleep(seconds * 1000);
            return new BeverageOrder(beverageOrder.orderId, beverageOrder.name, beverageOrder.beverage, Status.READY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return beverageOrder;
    }

}
