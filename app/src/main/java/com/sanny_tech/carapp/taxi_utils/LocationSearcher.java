package com.sanny_tech.carapp.taxi_utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.sanny_tech.carapp.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LocationSearcher {

    private Context context;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String key;

    public LocationSearcher(Context context, String key) {
        this.context = context;
        this.key = key;
        // Initialize PlacesClient
        if (!Places.isInitialized()) {
            Places.initialize(context, key);
        }
        placesClient = Places.createClient(context);
        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void checkLocationForPointOfInterest(boolean isPickup, double latitude, double longitude, LocationCallback callback) {
        Toast.makeText(context, "Fetching location", Toast.LENGTH_SHORT).show();

        String url = String.format(Locale.US,
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=50&type=point_of_interest|taxi_stand&key=%s",
                latitude, longitude, key);

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.getJSONArray("results");
                            if (results.length() > 0) {
                                JSONObject placeJson = results.getJSONObject(0);
                                String name = placeJson.getString("name");

                                // Optional: simulate a `Place` object if needed
                                Place place = Place.builder().setName(name).build();

                                if (callback != null) {
                                    callback.onLocationFound(place, isPickup);
                                }
                            } else {
                                if (callback != null) {
                                    callback.onLocationFound(null, isPickup);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (callback != null) {
                                callback.onLocationFound(null, isPickup);
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        if (callback != null) {
                            callback.onLocationFound(null, isPickup);
                        }
                    }
                });

        queue.add(jsonObjectRequest);
    }

    public interface LocationCallback {
        void onLocationFound(Place place, boolean isPickup);
    }
}

