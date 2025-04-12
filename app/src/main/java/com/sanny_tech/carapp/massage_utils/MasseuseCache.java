package com.sanny_tech.carapp.massage_utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sanny_tech.carapp.entities.Car;

import java.lang.reflect.Type;
import java.util.List;

public class MasseuseCache {
    private static final String PREF_KEY = "data";
    private static final String LAST_UPDATE_KEY = "update_time";

    public static void saveData(Context context, List<Masseuse> data) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        // Save the current timestamp as the last update time
        long currentTime = System.currentTimeMillis();
        editor.putLong(LAST_UPDATE_KEY, currentTime);

        String jsonData = gson.toJson(data);
        editor.putString("masseuse_data", jsonData);
        editor.apply();
    }

    public static List<Masseuse> loadData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonData = sharedPreferences.getString("masseuse_data", null);
        Type type = new TypeToken<List<Masseuse>>() {}.getType();
        return gson.fromJson(jsonData, type);
    }

    public static long getLastUpdateTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(LAST_UPDATE_KEY, 0);
    }
}


