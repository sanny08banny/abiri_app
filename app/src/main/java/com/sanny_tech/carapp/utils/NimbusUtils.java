package com.sanny_tech.carapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class NimbusUtils {

    public static String getNimbusId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("nimbus_prefs", Context.MODE_PRIVATE);
        String existingId = prefs.getString("nimbus_id", null);

        if (existingId != null) {
            return existingId;
        } else {
            String newId = UUID.randomUUID().toString();
            prefs.edit().putString("nimbus_id", newId).apply();
            return newId;
        }
    }
}
