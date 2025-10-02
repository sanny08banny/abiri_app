package com.sanny_tech.carapp.taxi_utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.sanny_tech.carapp.entities.SubscriptionPlan;

public class SubscriptionManager {
    private static final String PREFS_NAME = "SubscriptionPrefs";
    private static final String KEY_PLAN_NAME = "PlanName";
    private static final String KEY_PLAN_PRICE = "PlanPrice";
    private static final String KEY_PLAN_DESCRIPTION = "PlanDescription";
    private static final String KEY_PLAN_EXPIRY_DATE = "PlanExpiryDate";

    private SharedPreferences sharedPreferences;

    public SubscriptionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSubscriptionPlan(SubscriptionPlan plan) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PLAN_NAME, plan.getName());
        editor.putString(KEY_PLAN_PRICE, plan.getPrice());
        editor.putString(KEY_PLAN_DESCRIPTION, plan.getDescription());
        editor.putLong(KEY_PLAN_EXPIRY_DATE, plan.getExpiryDate());
        editor.apply();
    }

    public SubscriptionPlan getSubscriptionPlan() {
        long currentTime = System.currentTimeMillis();
        long expiryDate = sharedPreferences.getLong(KEY_PLAN_EXPIRY_DATE, 0);
        if (currentTime > expiryDate) {
            clearSubscriptionPlan(); // Delete expired plan
            return null;
        }
        String name = sharedPreferences.getString(KEY_PLAN_NAME, "No Plan Selected");
        String price = sharedPreferences.getString(KEY_PLAN_PRICE, "");
        String description = sharedPreferences.getString(KEY_PLAN_DESCRIPTION, "");
        return new SubscriptionPlan(name, price, description, expiryDate);
    }

    public void clearSubscriptionPlan() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}

