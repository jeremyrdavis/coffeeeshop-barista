package com.redhat.examples.quarkus;

import com.redhat.examples.quarkus.coffeeshop.barista.domain.Beverage;
import com.redhat.examples.quarkus.coffeeshop.barista.domain.BeverageOrder;
import com.redhat.examples.quarkus.coffeeshop.barista.domain.Status;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class BaristaTest {


    @Inject
    Barista barista;


    @Test
    public void testBlackCoffeeOrder() throws ExecutionException, InterruptedException {

        BeverageOrder beverageOrder = new BeverageOrder(UUID.randomUUID().toString(),"Jeremy", Beverage.BLACK_COFFEE);
        CompletableFuture<BeverageOrder> result = barista.orderIn(beverageOrder);
        assertEquals(result.get().status, Status.READY);
    }

    @Test
    public void testLatteOrder() throws ExecutionException, InterruptedException {

        BeverageOrder beverageOrder = new BeverageOrder(UUID.randomUUID().toString(),"Jeremy", Beverage.LATTE);
        CompletableFuture<BeverageOrder> result = barista.orderIn(beverageOrder);
        assertEquals(result.get().status, Status.READY);
    }
}
