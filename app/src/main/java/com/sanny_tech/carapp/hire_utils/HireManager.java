package com.sanny_tech.carapp.hire_utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HireManager {
    private DatabaseReference hireRef;
    private ValueEventListener hireListener;
    private Context context;
    private String carId;

    public HireManager(Context context, String carId) {
        this.context = context;
        this.carId = carId;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        hireRef = database.getReference("hires"); // Replace with your Firebase location reference
    }

    public void startRideUpdates(final OnHireChangedListener listener) {
        hireListener = hireRef.addValueEventListener(new ValueEventListener() {
            // Inside onDataChange method of ValueEventListener
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Hire hire = snapshot.getValue(Hire.class);
                        if (hire != null && hire.getClient_id().equals(getCurrentAccountId()) &&
                        hire.getCarId().equals(carId)) {
                            // Notify listener about the updated location
                            listener.onHireChanged(hire);
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
    public void stopHireUpdates() {
        if (hireListener != null) {
            hireRef.removeEventListener(hireListener);
        }
    }

    public interface OnHireChangedListener {
        void onHireChanged(Hire hire);
    }
}
