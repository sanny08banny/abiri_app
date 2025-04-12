package com.sanny_tech.carapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class JourneyStatusManager {

    private static final String KEY_JOURNEY_STARTED = "journey_started";
    private static final String KEY_JOURNEY_START_TIMESTAMP = "journey_start_timestamp";
    private static final String KEY_DESTINATION_LAT = "destination_lat";
    private static final String KEY_DESTINATION_LON = "destination_lon";

    private SharedPreferences sharedPreferences;

    public JourneyStatusManager(Context context) {
        sharedPreferences = context.getSharedPreferences("JourneyStatus", Context.MODE_PRIVATE);
    }

    public void setJourneyStarted(boolean isStarted) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_JOURNEY_STARTED, isStarted);
        if (isStarted) {
            // Save current timestamp if journey is started
            editor.putLong(KEY_JOURNEY_START_TIMESTAMP, System.currentTimeMillis());
        } else {
            // Clear timestamp if journey is not started
            editor.remove(KEY_JOURNEY_START_TIMESTAMP);
        }
        editor.apply();
    }

    public boolean isJourneyStarted() {
        return sharedPreferences.getBoolean(KEY_JOURNEY_STARTED, false);
    }

    public void setDestination(double latitude, double longitude) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(KEY_DESTINATION_LAT, (float) latitude);
        editor.putFloat(KEY_DESTINATION_LON, (float) longitude);
        editor.apply();
    }

    public LatLng getDestination() {
        double destinationLat = sharedPreferences.getFloat(KEY_DESTINATION_LAT, 0.0f);
        double destinationLon = sharedPreferences.getFloat(KEY_DESTINATION_LON, 0.0f);
        return new LatLng(destinationLat, destinationLon);
    }

    public long getJourneyStartTimestamp() {
        return sharedPreferences.getLong(KEY_JOURNEY_START_TIMESTAMP, 0L);
    }

    public Date getJourneyStartDate() {
        long timestamp = getJourneyStartTimestamp();
        return new Date(timestamp);
    }

    public long getElapsedTripTime() {
        long startTime = getJourneyStartTimestamp();
        if (startTime > 0) {
            return System.currentTimeMillis() - startTime;
        } else {
            return 0;
        }
    }

    public String getFormattedElapsedTime() {
        long elapsedMillis = getElapsedTripTime();
        long seconds = elapsedMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        // Format the elapsed time into a readable string
        String formattedTime = String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
        return formattedTime;
    }
}


