package com.sanny_tech.carapp.taxi_utils;

import java.util.ArrayList;
import java.util.List;

public class TaxiRequest{
    private PricingDetails pricing_details;
    private String dest_name;
    private Double price;
    private List<String> declined = new ArrayList<>();
    private String phone_number;
    private String taxi_category;

    public TaxiRequest() {
    }

    public PricingDetails getPricing_details() {
        return pricing_details;
    }

    public void setPricing_details(PricingDetails pricing_details) {
        this.pricing_details = pricing_details;
    }

    public String getDest_name() {
        return dest_name;
    }

    public void setDest_name(String dest_name) {
        this.dest_name = dest_name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<String> getDeclined() {
        return declined;
    }

    public void setDeclined(List<String> declined) {
        this.declined = declined;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getTaxi_category() {
        return taxi_category;
    }

    public void setTaxi_category(String taxi_category) {
        this.taxi_category = taxi_category;
    }

    @Override
    public String toString() {
        return "TaxiRequest{" +
                "pricing_details=" + pricing_details +
                ", dest_name='" + dest_name + '\'' +
                ", price=" + price +
                ", declined=" + declined +
                ", phone_number='" + phone_number +
                ", category='" + taxi_category +'\'' +
                '}';
    }
}
