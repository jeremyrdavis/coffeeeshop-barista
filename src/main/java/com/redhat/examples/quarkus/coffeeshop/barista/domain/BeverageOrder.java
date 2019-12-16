package com.redhat.examples.quarkus.coffeeshop.barista.domain;

public class BeverageOrder {


    public String name;

    public Beverage beverage;

    public Status status;

    public BeverageOrder() {
    }

    public BeverageOrder(String name, Beverage beverage) {
        this.name = name;
        this.beverage = beverage;
    }

    public BeverageOrder(String name, Beverage beverage, Status status) {
        this.name = name;
        this.beverage = beverage;
        this.status = status;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("BeverageOrder[")
                .append("name=")
                .append(name)
                .append(",beverage=")
                .append(beverage)
                .append(",status=")
                .append(status)
                .append("]")
                .toString();
    }
}
