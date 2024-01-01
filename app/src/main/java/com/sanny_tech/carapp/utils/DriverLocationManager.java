package com.sanny_tech.carapp.utils;

import com.sanny_tech.carapp.entities.Ride;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DriverLocationManager {
    private DatabaseReference locationRef;
    private ValueEventListener locationListener;

    public DriverLocationManager() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        locationRef = database.getReference("taxi_rides"); // Replace with your Firebase location reference
    }

    public void startLocationUpdates(final OnLocationChangedListener listener) {
        locationListener = locationRef.addValueEventListener(new ValueEventListener() {
            // Inside onDataChange method of ValueEventListener
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Ride ride = snapshot.getValue(Ride.class);
                        if (ride != null && ride.getDriver_lat() != 0) {
                            Double latitude = (double) ride.getDriver_lat();
                            Double longitude = (double) ride.getDriver_lon();

                            // Notify listener about the updated location
                            listener.onLocationChanged(latitude, longitude);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    public void stopLocationUpdates() {
        if (locationListener != null) {
            locationRef.removeEventListener(locationListener);
        }
    }

    public interface OnLocationChangedListener {
        void onLocationChanged(Double latitude, Double longitude);
    }
}
