package com.sanny_tech.carapp.entities;

public class Decline {
    private String driver_id;
    private String client_id;

    public Decline() {
    }

    public Decline(String driver_id, String client_id) {
        this.driver_id = driver_id;
        this.client_id = client_id;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }
}
