package com.sanny_tech.carapp.taxi_utils;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class RouteCalculator {

    private static final String DIRECTIONS_API_BASE_URL = "https://maps.googleapis.com/maps/api/directions/";
    private Retrofit retrofit;
    private DirectionsService directionsService;
    private Context context;
    private String apiKey;

    public RouteCalculator(Context context) {
        this.context = context;
        this.retrofit = new Retrofit.Builder()
                .baseUrl(DIRECTIONS_API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.directionsService = retrofit.create(DirectionsService.class);
    }

    public interface DirectionsService {
        @GET("json")
        Call<JsonObject> getDirections(
                @Query("origin") String origin,
                @Query("destination") String destination,
                @Query("key") String apiKey
        );
    }

    public void calculateTravelTimes(LatLng driverLocation, LatLng pickupLocation, LatLng destination, final TravelDetailsCallback callback) {
        FirebaseHelper firebaseHelper = new FirebaseHelper(new FirebaseHelper.MapKeyCallback() {
            @Override
            public void onMapKeyReceived(String mapKey) {
                apiKey = mapKey;
                if (apiKey != null) {
                    String driverToPickup = driverLocation.latitude + "," + driverLocation.longitude;
                    String pickupToDestination = pickupLocation.latitude + "," + pickupLocation.longitude;

                    // Calculate details from driver location to pickup location
                    directionsService.getDirections(driverToPickup, pickupLocation.latitude + "," + pickupLocation.longitude, apiKey)
                            .enqueue(new retrofit2.Callback<JsonObject>() {
                                @Override
                                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        JsonObject json = response.body();
                                        TravelDetails driverToPickupDetails = extractTravelDetailsFromJson(json);

                                        // Calculate details from pickup location to destination
                                        directionsService.getDirections(pickupToDestination, destination.latitude + "," + destination.longitude, apiKey)
                                                .enqueue(new retrofit2.Callback<JsonObject>() {
                                                    @Override
                                                    public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                                                        if (response.isSuccessful() && response.body() != null) {
                                                            JsonObject json = response.body();
                                                            TravelDetails pickupToDestinationDetails = extractTravelDetailsFromJson(json);
                                                            callback.onTravelDetailsCalculated(driverToPickupDetails, pickupToDestinationDetails);
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<JsonObject> call, Throwable t) {
                                                        callback.onError(t);
                                                    }
                                                });
                                    }
                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                    callback.onError(t);
                                }
                            });
                } else {
                    callback.onError(new Throwable("API key not found"));
                }
            }

            @Override
            public void onError(DatabaseError error) {
                callback.onError(new Throwable(error.getMessage()));
            }
        });
        firebaseHelper.fetchMapKey();
    }
    public void calculateSingleTravelDetails(LatLng origin, LatLng destination, final SingleTravelDetailsCallback callback) {
        FirebaseHelper firebaseHelper = new FirebaseHelper(new FirebaseHelper.MapKeyCallback() {
            @Override
            public void onMapKeyReceived(String mapKey) {
                apiKey = mapKey;
                if (apiKey != null) {
                    String originStr = origin.latitude + "," + origin.longitude;
                    String destinationStr = destination.latitude + "," + destination.longitude;

                    // Calculate details from origin to destination
                    directionsService.getDirections(originStr, destinationStr, apiKey)
                            .enqueue(new retrofit2.Callback<JsonObject>() {
                                @Override
                                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        JsonObject json = response.body();
                                        TravelDetails travelDetails = extractTravelDetailsFromJson(json);
                                        callback.onSingleTravelDetailsCalculated(travelDetails);
                                    } else {
                                        callback.onError(new Throwable("Failed to get response"));
                                    }
                                }

                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                    callback.onError(t);
                                }
                            });
                } else {
                    callback.onError(new Throwable("API key not found"));
                }
            }

            @Override
            public void onError(DatabaseError error) {
                callback.onError(new Throwable(error.getMessage()));
            }
        });
        firebaseHelper.fetchMapKey();
    }

    private TravelDetails extractTravelDetailsFromJson(JsonObject json) {
        try {
            JsonObject legs = json.getAsJsonArray("routes")
                    .get(0).getAsJsonObject()
                    .getAsJsonArray("legs")
                    .get(0).getAsJsonObject();

            int duration = legs.getAsJsonObject("duration").get("value").getAsInt();
            int distance = legs.getAsJsonObject("distance").get("value").getAsInt();
            return new TravelDetails(duration, distance);
        } catch (Exception e) {
            return new TravelDetails(0, 0);
        }
    }

    public interface TravelDetailsCallback {
        void onTravelDetailsCalculated(TravelDetails driverToPickupDetails, TravelDetails pickupToDestinationDetails);

        void onError(Throwable throwable);
    }
    public interface SingleTravelDetailsCallback {
        void onSingleTravelDetailsCalculated(TravelDetails travelDetails);

        void onError(Throwable throwable);
    }
}


