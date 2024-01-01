package com.sanny_tech.carapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;

public class JourneyStatusManager {

    private static final String KEY_JOURNEY_STARTED = "journey_started";
    private static final String KEY_DESTINATION_LAT = "destination_lat";
    private static final String KEY_DESTINATION_LON = "destination_lon";

    private SharedPreferences sharedPreferences;

    public JourneyStatusManager(Context context) {
        sharedPreferences = context.getSharedPreferences("JourneyStatus", Context.MODE_PRIVATE);
    }

    public void setJourneyStarted(boolean isStarted) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_JOURNEY_STARTED, isStarted);
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
}


