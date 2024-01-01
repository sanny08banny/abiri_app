package com.sanny_tech.carapp.entities;

public class TaxiLocation {
    private String driverId;
    private double seats;
    private double longitude;
    private double latitude;
    private String status;

    public TaxiLocation() {
    }
    public TaxiLocation(String driverId, double seats, double longitude, double latitude, String status) {
        this.driverId = driverId;
        this.seats = seats;
        this.longitude = longitude;
        this.latitude = latitude;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
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

    public double getSeats() {
        return seats;
    }

    public void setSeats(double seats) {
        this.seats = seats;
    }
}
