package com.sanny_tech.carapp.taxi_utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LocationSearcher {

    private Context context;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;

    public LocationSearcher(Context context) {
        this.context = context;
        // Initialize PlacesClient
        if (!Places.isInitialized()) {
            Places.initialize(context, "AIzaSyAlGhvKajzrEZiLaY0XfF-yoPzQnxuKtGM");
        }
        placesClient = Places.createClient(context);
        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void checkLocationForPointOfInterest(boolean isPickup,double latitude, double longitude, LocationCallback callback) {
        Toast.makeText(context, "Fetching location", Toast.LENGTH_SHORT).show();
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                // Check the address type using Google Places API
                FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(Arrays.asList(Place.Field.NAME, Place.Field.TYPES));

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                placesClient.findCurrentPlace(request).addOnSuccessListener(new OnSuccessListener<FindCurrentPlaceResponse>() {
                    @Override
                    public void onSuccess(FindCurrentPlaceResponse response) {
                        for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                            Place place = placeLikelihood.getPlace();
                            List<Place.Type> placeTypes = place.getTypes();

                            // Check if the place is a point of interest or a taxi stand
                            if (placeTypes != null && (placeTypes.contains(Place.Type.POINT_OF_INTEREST) ||
                                    placeTypes.contains(Place.Type.TAXI_STAND))) {
                                // This is a point of interest (landmark or potential taxi destination)
                                if (callback != null) {
                                    callback.onLocationFound(place, isPickup);
                                }
                                return; // Exit the loop if a point of interest is found
                            }
                        }
                        // If no point of interest found among the likely places
                        if (callback != null) {
                            callback.onLocationFound(null, false);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Handle failure in finding current place
                        if (callback != null) {
                            callback.onLocationFound(null, false);
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onLocationFound(null, false);
            }
        }
    }

    public interface LocationCallback {
        void onLocationFound(Place place, boolean isPickup);
    }
}

