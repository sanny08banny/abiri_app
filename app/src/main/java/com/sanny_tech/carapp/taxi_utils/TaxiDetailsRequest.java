package com.sanny_tech.carapp.taxi_utils;

public class TaxiDetailsRequest {
    private String taxi_id;

    public TaxiDetailsRequest() {
    }

    public TaxiDetailsRequest(String taxi_id) {
        this.taxi_id = taxi_id;
    }

    public String getTaxi_id() {
        return taxi_id;
    }

    public void setTaxi_id(String taxi_id) {
        this.taxi_id = taxi_id;
    }
}
