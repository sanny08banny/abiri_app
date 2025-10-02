package com.sanny_tech.carapp.taxi_utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sanny_tech.carapp.entities.TaxiLocation;

import java.util.ArrayList;
import java.util.List;

public class TaxiViewModel extends ViewModel {
    private final MutableLiveData<List<TaxiLocation>> taxiLocations = new MutableLiveData<>();
    private static final double MAX_DISTANCE_METERS = 5000; // 5000 meters as an example

    public LiveData<List<TaxiLocation>> getTaxiLocations() {
        return taxiLocations;
    }

    public void loadTaxiLocations(double userLatitude, double userLongitude) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("taxi_locations");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<TaxiLocation> taxis = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TaxiLocation taxiLocation = snapshot.getValue(TaxiLocation.class);
                    if (taxiLocation != null && isWithinDistance(userLatitude, userLongitude,
                            taxiLocation.getLatitude(), taxiLocation.getLongitude()) &&
                            taxiLocation.getStatus().equals("available")) {
                        taxis.add(taxiLocation);
                    }
                }
                taxiLocations.setValue(taxis);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
    }
    private boolean isWithinDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = R * c; // Distance in kilometers

        // Convert distance to meters if required
        double distanceInMeters = distance * 1000;

        return distanceInMeters <= MAX_DISTANCE_METERS;
    }
}
