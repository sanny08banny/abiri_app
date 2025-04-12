package com.sanny_tech.carapp.taxi_utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

public class DriverAvailabilityManager {

    private static final String PREF_NAME = "AvailabilityPrefs";
    private static final String KEY_STATUS = "availabilityStatus";
    private static final String KEY_TAXI_DETAILS = "taxiDetails";
    private static final String TAG = "TaxiPreferencesManager";


    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Gson gson;

    public DriverAvailabilityManager(Context context) {
        this.gson = new Gson();
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    // Save availability status
    public void saveAvailabilityStatus(boolean status) {
        editor.putBoolean(KEY_STATUS, status);
        editor.apply();
    }

    // Get availability status
    public boolean getAvailabilityStatus() {
        return sharedPreferences.getBoolean(KEY_STATUS, false);
    }
    public void saveTaxiInit(TaxiInit taxiInit) {
        String taxiDetailsJson = gson.toJson(taxiInit);
        editor.putString(KEY_TAXI_DETAILS, taxiDetailsJson);
        boolean success = editor.commit();
        if (success) {
            Log.d(TAG, "TaxiInit details saved successfully");
        } else {
            Log.d(TAG, "Failed to save TaxiInit details");
        }
    }

    // Save taxi details
    public void deleteTaxiInit() {
        editor.remove(KEY_TAXI_DETAILS);
        boolean success = editor.commit();
        if (success) {
            Log.d(TAG, "TaxiInit details removed successfully");
        } else {
            Log.d(TAG, "Failed to remove TaxiInit details");
        }
    }


    // Get taxi details
    public TaxiInit getTaxiInit() {
        String taxiDetailsJson = sharedPreferences.getString(KEY_TAXI_DETAILS, null);
        if (taxiDetailsJson == null) {
            return null; // or you could return a new TaxiDetailsDTO() with default values
        }
        return gson.fromJson(taxiDetailsJson, TaxiInit.class);
    }
}



