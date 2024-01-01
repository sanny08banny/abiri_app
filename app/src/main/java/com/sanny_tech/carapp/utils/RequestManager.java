package com.sanny_tech.carapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.sanny_tech.carapp.taxi_utils.ClientRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class RequestManager {
    private static final String PREF_KEY = "cached_data";

    public static void saveRequest(Context context, List<ClientRequest> data) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String jsonData = gson.toJson(data);
        editor.putString("request", jsonData);
        editor.apply();
    }
    public static void clearRequests(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        editor.remove(PREF_KEY);
        editor.apply();
    }

    public static List<ClientRequest> loadRequest(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonData = sharedPreferences.getString("request", null);
        Type type = new TypeToken<List<ClientRequest>>() {}.getType();
        return gson.fromJson(jsonData, type);
    }

}


