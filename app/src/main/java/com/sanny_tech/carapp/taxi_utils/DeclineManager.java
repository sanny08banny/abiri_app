package com.sanny_tech.carapp.taxi_utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.sanny_tech.carapp.entities.Decline;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DeclineManager {
    private DatabaseReference locationRef;
    private ValueEventListener locationListener;
    private Context context;

    public DeclineManager(Context context) {
        this.context = context;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        locationRef = database.getReference("declines"); // Replace with your Firebase location reference
    }

    public void startRideUpdates(final OnDeclineListener listener) {
        locationListener = locationRef.addValueEventListener(new ValueEventListener() {
            // Inside onDataChange method of ValueEventListener
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Decline decline = snapshot.getValue(Decline.class);
                        if (decline != null && decline.getClient_id().equals(getCurrentAccountId())) {
                            // Notify listener about the updated location
                            listener.onDecline(decline);
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
    public void stopRideUpdates() {
        if (locationListener != null) {
            locationRef.removeEventListener(locationListener);
        }
    }

    public interface OnDeclineListener {
        void onDecline(Decline decline);
    }
}
