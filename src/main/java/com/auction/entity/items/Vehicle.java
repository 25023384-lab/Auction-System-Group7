package com.auction.entity.items;

import java.time.LocalDateTime;

public class Vehicle extends Item {
    private int engineCC;

    // Default constructor for Jackson
    public Vehicle() {
        super();
        this.setType("VEHICLE");
    }

    public Vehicle(String id, String name, String description, double startingPrice,
                   LocalDateTime startTime, LocalDateTime endTime, String sellerId, int engineCC) {
        super(id, name, description, startingPrice, startTime, endTime, sellerId);
        this.engineCC = engineCC;
        this.setType("VEHICLE");
    }

    public Vehicle(String id, String name, String description, double startingPrice,
                   LocalDateTime startTime, LocalDateTime endTime, int engineCC) {
        this(id, name, description, startingPrice, startTime, endTime, null, engineCC);
    }

    public int getEngineCC() {
        return engineCC;
    }

    public void setEngineCC(int engineCC) {
        this.engineCC = engineCC;
    }

    @Override
    public void printInfo() {
        System.out.println("[Vehicle] " + getName() + " | Start: $" + getStartingPrice() + " | Engine: " + engineCC + " cc");
    }
}
