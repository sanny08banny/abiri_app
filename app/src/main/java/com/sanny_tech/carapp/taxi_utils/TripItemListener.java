package com.sanny_tech.carapp.taxi_utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TripItemListener {
    private DatabaseReference hireRef;
    private ValueEventListener hireListener;
    private Context context;
    private String carId;

    public TripItemListener(Context context) {
        this.context = context;
        this.carId = carId;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        hireRef = database.getReference("trips"); // Replace with your Firebase location reference
    }

    public void startTripUpdates(final OnTripChangedListener listener) {
        hireListener = hireRef.addValueEventListener(new ValueEventListener() {
            // Inside onDataChange method of ValueEventListener
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Trip trip = snapshot.getValue(Trip.class);
                        if (trip != null && trip.getUser_id().equals(getCurrentAccountId()) &&
                        !trip.getEnd_time().equals("")) {
                            // Notify listener about the updated location
                            listener.onTripChanged(trip);
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
    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
    public void stopTripUpdates() {
        if (hireListener != null) {
            hireRef.removeEventListener(hireListener);
        }
    }

    public interface OnTripChangedListener {
        void onTripChanged(Trip trip);
    }
}


