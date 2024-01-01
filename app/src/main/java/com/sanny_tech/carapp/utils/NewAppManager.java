package com.sanny_tech.carapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class NewAppManager {

    private static final String PREF_NAME = "MyAppPrefs";
    private static final String NEW_APP = "new_app";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean getNewApp(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        // Retrieve the boolean value, defaulting to false if not found
        return sharedPreferences.getBoolean(NEW_APP, true);
    }

    public static void setNewApp(Context context, boolean newTaxiStatus) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(NEW_APP, newTaxiStatus);
        editor.apply();
    }
}


