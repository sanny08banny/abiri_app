package com.sanny_tech.carapp.taxi_utils;

public class DriverResponse {
    private String client_id;
    private String driver_id;

    public DriverResponse() {
    }

    public DriverResponse(String client_id, String driverId) {
        this.client_id = client_id;
        driver_id = driverId;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }
}
