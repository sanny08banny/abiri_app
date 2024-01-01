package com.sanny_tech.carapp.taxi_utils;

public class TaxiRequest {
    private String client_id;
    private String recepient_id;
    private float dest_lat,dest_lon;
    private float current_lat,current_lon;

    public TaxiRequest() {
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getRecepient_id() {
        return recepient_id;
    }

    public void setRecepient_id(String recepient_id) {
        this.recepient_id = recepient_id;
    }

    public float getDest_lat() {
        return dest_lat;
    }

    public void setDest_lat(float dest_lat) {
        this.dest_lat = dest_lat;
    }

    public float getDest_lon() {
        return dest_lon;
    }

    public void setDest_lon(float dest_lon) {
        this.dest_lon = dest_lon;
    }

    public float getCurrent_lat() {
        return current_lat;
    }

    public void setCurrent_lat(float current_lat) {
        this.current_lat = current_lat;
    }

    public float getCurrent_lon() {
        return current_lon;
    }

    public void setCurrent_lon(float current_lon) {
        this.current_lon = current_lon;
    }
}
