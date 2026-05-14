package com.auction.entity;

import java.time.LocalDateTime;

public class Vehicle extends Item {
    private String make; // Hãng sản xuất
    private String model; // Mẫu xe
    private int year; // Năm sản xuất

    // Constructor không tham số cho Jackson
    public Vehicle() {
        super();
        this.setType("VEHICLE");
    }

    // Constructor đầy đủ
    public Vehicle(String id, String name, String description, double startingPrice,
                   LocalDateTime startTime, LocalDateTime endTime, String sellerId,
                   String make, String model, int year) {
        super(id, name, description, startingPrice, startTime, endTime, sellerId);
        this.make = make;
        this.model = model;
        this.year = year;
        this.setType("VEHICLE");
    }

    // Getters and Setters cho các thuộc tính riêng
    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
    public void printInfo(){
        System.out.println("[Vehicles] " + getModel() + " by " + getName() + "released: " + getYear() + " | Start: $" + getStartingPrice());
    }
}