package com.sanny_tech.carapp.taxi_utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class LocationSearcher {

    private static final String TAG = "LocationSearcher";

    private Context context;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String key;

    public LocationSearcher(Context context, String key) {
        this.context = context;
        this.key = key;

        Log.d(TAG, "Initializing PlacesClient");
        if (!Places.isInitialized()) {
            Places.initialize(context, key);
            Log.d(TAG, "Places initialized");
        } else {
            Log.d(TAG, "Places already initialized");
        }

        placesClient = Places.createClient(context);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        Log.d(TAG, "LocationSearcher initialized");
    }

    public void checkLocationForPointOfInterest(boolean isPickup, double latitude, double longitude, LocationCallback callback) {
        Toast.makeText(context, "Fetching location", Toast.LENGTH_SHORT).show();
        fetchNearbyPlace(isPickup, latitude, longitude, callback, 150, "point_of_interest|taxi_stand", false);
    }
    private void fetchNearbyPlace(
            boolean isPickup, double latitude, double longitude, LocationCallback callback,
            int radius, String types, boolean isRetry
    ) {
        String url = String.format(Locale.US,
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=%d&type=%s&key=%s",
                latitude, longitude, radius, types, key);

        Log.d(TAG, "Requesting nearby place: " + url);

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d(TAG, "Volley response received: " + response.toString());
                    try {
                        JSONArray results = response.getJSONArray("results");
                        Log.d(TAG, "Results length: " + results.length());

                        if (results.length() > 0) {
                            JSONObject placeJson = results.getJSONObject(0);
                            String name = placeJson.getString("name");
                            Log.d(TAG, "Place found: " + name);

                            Place place = Place.builder().setName(name).build();

                            if (callback != null) {
                                callback.onLocationFound(place, isPickup);
                            }
                        } else if (!isRetry) {
                            Log.d(TAG, "No places found nearby. Retrying with broader search...");
                            // Retry with broader type and larger radius
                            fetchNearbyPlace(isPickup, latitude, longitude, callback, 400,
                                    "establishment|store|bus_station|point_of_interest", true);
                        } else {
                            Log.d(TAG, "No places found even after retry.");
                            if (callback != null) {
                                callback.onLocationFound(null, isPickup);
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error", e);
                        if (callback != null) {
                            callback.onLocationFound(null, isPickup);
                        }
                    }
                },
                error -> {
                    Log.e(TAG, "Volley request error", error);
                    if (callback != null) {
                        callback.onLocationFound(null, isPickup);
                    }
                });

        queue.add(jsonObjectRequest);
    }

    public interface LocationCallback {
        void onLocationFound(Place place, boolean isPickup);
    }
}
