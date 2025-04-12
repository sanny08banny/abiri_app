package com.sanny_tech.carapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.sanny_tech.carapp.taxi_utils.ClientRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

public class RequestManager {

    private static final String PREFS_NAME = "ClientRequestPrefs";
    private static final String REQUEST_KEY = "ClientRequestKey";

    private SharedPreferences sharedPreferences;

    public RequestManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveRequest(ClientRequest request) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Parcel parcel = Parcel.obtain();
        request.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();

        String serializedRequest = Base64.encodeToString(bytes, Base64.DEFAULT);
        editor.putString(REQUEST_KEY, serializedRequest);
        editor.apply();
    }

    public ClientRequest loadRequest() {
        String serializedRequest = sharedPreferences.getString(REQUEST_KEY, null);
        if (serializedRequest != null) {
            byte[] bytes = Base64.decode(serializedRequest, Base64.DEFAULT);
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(bytes, 0, bytes.length);
            parcel.setDataPosition(0);
            ClientRequest request = ClientRequest.CREATOR.createFromParcel(parcel);
            parcel.recycle();
            return request;
        }
        return null;
    }

    public void clearRequest() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(REQUEST_KEY);
        editor.apply();
    }
}




