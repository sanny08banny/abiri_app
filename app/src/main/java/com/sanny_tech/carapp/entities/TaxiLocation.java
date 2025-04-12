package com.sanny_tech.carapp.entities;

import com.sanny_tech.carapp.taxi_utils.TaxiInit;
import com.sanny_tech.carapp.taxi_utils.TaxisAvailable;

public class TaxiLocation {
    private String driverId;
    private int seats;
    private double longitude;
    private double latitude;
    private String status;
    private float orientation;
    private String category;
    private PayLink payLink;
    private TaxiInit taxiInit;

    public TaxiLocation() {
    }
    public TaxiLocation(String driverId, int seats, double longitude, double latitude,
                        String status, float orientation, PayLink payLink,
                        TaxiInit taxiInit) {
        this.driverId = driverId;
        this.seats = seats;
        this.longitude = longitude;
        this.latitude = latitude;
        this.status = status;
        this.orientation = orientation;
        this.payLink = payLink;
        this.taxiInit = taxiInit;
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

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public float getOrientation() {
        return orientation;
    }

    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }

    public PayLink getPayLink() {
        return payLink;
    }

    public void setPayLink(PayLink payLink) {
        this.payLink = payLink;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public TaxiInit getTaxiInit() {
        return taxiInit;
    }

    public void setTaxiInit(TaxiInit taxiInit) {
        this.taxiInit = taxiInit;
    }

    public TaxisAvailable createTaxiAvailble() {
        return new TaxisAvailable(driverId,longitude,latitude,orientation,
                TaxiCategory.getNumberOfSeats(taxiInit.getCategory()));
    }
}
