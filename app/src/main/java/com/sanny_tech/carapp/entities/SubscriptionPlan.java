package com.sanny_tech.carapp.entities;

public class SubscriptionPlan {
    private String name;
    private String price;
    private String description;
    private long expiryDate; // Store as Unix timestamp

    public SubscriptionPlan(String name, String price, String description, long expiryDate) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.expiryDate = expiryDate;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public long getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(long expiryDate) {
        this.expiryDate = expiryDate;
    }
}