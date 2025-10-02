package com.sanny_tech.carapp.taxi_utils;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class PricingDetails{
    private String rider_id;
    private double pick_up_latitude,pick_up_longitude;
    private double dest_latitude,dest_longitude;

    public PricingDetails() {
    }

    public PricingDetails(String rider_id, double pick_up_latitude, double pick_up_longitude,
                          double dest_latitude, double dest_longitude) {
        this.rider_id = rider_id;
        this.pick_up_latitude = pick_up_latitude;
        this.pick_up_longitude = pick_up_longitude;
        this.dest_latitude = dest_latitude;
        this.dest_longitude = dest_longitude;
    }

    public String getRider_id() {
        return rider_id;
    }

    public void setRider_id(String rider_id) {
        this.rider_id = rider_id;
    }

    public double getPick_up_latitude() {
        return pick_up_latitude;
    }

    public void setPick_up_latitude(double pick_up_latitude) {
        this.pick_up_latitude = pick_up_latitude;
    }

    public double getPick_up_longitude() {
        return pick_up_longitude;
    }

    public void setPick_up_longitude(double pick_up_longitude) {
        this.pick_up_longitude = pick_up_longitude;
    }

    public double getDest_latitude() {
        return dest_latitude;
    }

    public void setDest_latitude(double dest_latitude) {
        this.dest_latitude = dest_latitude;
    }

    public double getDest_longitude() {
        return dest_longitude;
    }

    public void setDest_longitude(double dest_longitude) {
        this.dest_longitude = dest_longitude;
    }

    @Override
    public String toString() {
        return "PricingDetails{" +
                "rider_id='" + rider_id + '\'' +
                ", pick_up_latitude=" + pick_up_latitude +
                ", pick_up_longitude=" + pick_up_longitude +
                ", dest_latitude=" + dest_latitude +
                ", dest_longitude=" + dest_longitude + '\'' +
                '}';
    }
}