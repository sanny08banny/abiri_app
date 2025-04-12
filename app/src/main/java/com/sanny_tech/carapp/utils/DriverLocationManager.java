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
    private OnTripStartListener trip_listener;
    private int notification_count = 0;
    private OnTripCancelListener cancel_listener;

    public DriverLocationManager() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        locationRef = database.getReference("taxi_rides"); // Replace with your Firebase location reference
    }

    public void startLocationUpdates(final OnLocationChangedListener listener, String currentAccountId) {
        locationListener = locationRef.addValueEventListener(new ValueEventListener() {
            // Inside onDataChange method of ValueEventListener
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Ride ride = snapshot.getValue(Ride.class);
                        if (ride != null && ride.getUser_id().equals(currentAccountId) && ride.getDriver_lat() != 0) {
                            Double latitude = (double) ride.getDriver_lat();
                            Double longitude = (double) ride.getDriver_lon();

                            // Notify listener about the updated location
                            listener.onLocationChanged(latitude, longitude);
                            if (ride.getStatus() != null && !ride.getStatus().equals("")) {
                                if (ride.getStatus().equals("waiting") && notification_count == 0) {
                                    trip_listener.onTripStart(true,ride.getDestination().getLatitude(),
                                            ride.getDestination().getLongitude());
                                    notification_count++;
                                } else if (ride.getStatus().equals("started") &&
                                        notification_count == 1) {
                                    trip_listener.onTripStart(false,ride.getDestination().getLatitude(),
                                            ride.getDestination().getLongitude());
                                    notification_count++;
                                }else if (ride.getStatus().equals("started") &&
                                        notification_count == 2){
                                    trip_listener.onTripStart(null,ride.getDestination().getLatitude(),
                                            ride.getDestination().getLongitude());
                                }else if (ride.getStatus().equals("canceled")){
                                    cancel_listener.onTripCancel(true,ride.getClientNumber());
                                }
                            }
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

    public void setOnTripStartListener(OnTripStartListener listener) {
        this.trip_listener = listener;
    }

    public interface OnTripStartListener {
        void onTripStart(Boolean isStarted, Double latitude, Double longitude);
    }
    public void setOnTripCancelListener(OnTripCancelListener listener) {
        this.cancel_listener = listener;
    }

    public interface OnTripCancelListener {
        void onTripCancel(Boolean isStarted, String clientNumber);
    }
}

