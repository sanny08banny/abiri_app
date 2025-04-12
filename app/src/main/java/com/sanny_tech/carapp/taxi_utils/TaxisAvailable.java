package com.sanny_tech.carapp.taxi_utils;

public class TaxisAvailable {
    private String driver_id;
    private double longitude;
    private double latitude;
    private float orientation;
    private int seats;

    public TaxisAvailable() {
    }

    public TaxisAvailable(String driver_id, double longitude, double latitude, float orientation, int seats) {
        this.driver_id = driver_id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.orientation = orientation;
        this.seats = seats;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public float getOrientation() {
        return orientation;
    }

    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

}
