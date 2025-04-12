package com.sanny_tech.carapp.taxi_utils;
public  class TravelDetails {
    private final int duration; // in seconds
    private final int distance; // in meters

    public TravelDetails(int duration, int distance) {
        this.duration = duration;
        this.distance = distance;
    }

    public int getDuration() {
        return duration;
    }

    public int getDistance() {
        return distance;
    }

    public String getReadableDuration() {
        int hours = duration / 3600;
        int minutes = (duration % 3600) / 60;
        if (hours > 0) {
            return String.format("%d hrs %d mins", hours, minutes);
        } else {
            return String.format("%d mins", minutes);
        }
    }

    public String getReadableDistance() {
        if (distance >= 1000) {
            return String.format("%.2f km", distance / 1000.0);
        } else {
            return String.format("%d m", distance);
        }
    }
}