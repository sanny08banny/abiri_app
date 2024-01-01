package com.sanny_tech.carapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SimCardManager {

    private static final String PREF_NAME = "sim_card_pref";
    private static final String KEY_PHONE_NUMBERS = "phone_number";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String getPhoneNumber(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getString(KEY_PHONE_NUMBERS, "");
    }

    public static void setPhoneNumber(Context context, String newNumber) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(KEY_PHONE_NUMBERS, newNumber);
        editor.apply();
    }
}

