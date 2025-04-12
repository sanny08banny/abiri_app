package com.sanny_tech.carapp.taxi_utils;

import com.sanny_tech.carapp.entities.TaxiLocation;

import java.util.ArrayList;
import java.util.List;

public class Vehicle {
    private String category;
    private int seat_count;
    private double price;
    private List<TaxiLocation> taxiLocations = new ArrayList<>();

    public Vehicle(String category, int seat_count) {
        this.category = category;
        this.seat_count = seat_count;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getSeat_count() {
        return seat_count;
    }

    public void setSeat_count(int seat_count) {
        this.seat_count = seat_count;
    }

    public List<TaxiLocation> getTaxiLocations() {
        return taxiLocations;
    }

    public void setTaxiLocations(List<TaxiLocation> taxiLocations) {
        this.taxiLocations = taxiLocations;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void addTaxiLocation(TaxiLocation location) {
        taxiLocations.add(location);
    }
}
