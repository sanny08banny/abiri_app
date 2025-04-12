package com.sanny_tech.carapp.taxi_utils;

import java.util.ArrayList;

public class TaxiDetails {
    private String category;
    private String color;
    private String driver_id;
    private String manufacturer;
    private String model;
    private String plate_number;
    private ArrayList<String> taxi_images;

    public TaxiDetails() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPlate_number() {
        return plate_number;
    }

    public void setPlate_number(String plate_number) {
        this.plate_number = plate_number;
    }

    public ArrayList<String> getTaxi_images() {
        return taxi_images;
    }

    public void setTaxi_images(ArrayList<String> taxi_images) {
        this.taxi_images = taxi_images;
    }
}
