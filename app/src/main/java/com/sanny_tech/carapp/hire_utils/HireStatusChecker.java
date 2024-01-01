package com.sanny_tech.carapp.hire_utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HireStatusChecker {
    private DatabaseReference hireRef;
    private ValueEventListener hireListener;
    private Context context;
    private List<String> carIds;

    public HireStatusChecker(Context context, List<String> cars) {
        this.context = context;
        this.carIds = cars;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        hireRef = database.getReference("hires"); // Replace with your Firebase location reference
    }

    public void startRideUpdates(final OnHireChangedListener listener) {
        hireListener = hireRef.addValueEventListener(new ValueEventListener() {
            // Inside onDataChange method of ValueEventListener
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Hire> hires = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Hire hire = snapshot.getValue(Hire.class);
                        if (hire != null && hire.getOwner_id().equals(getCurrentAccountId())) {
                            // Notify listener about the updated location
                            hires.add(hire);
                        }
                    }
                    List<String> hiredIds = new ArrayList<>();
                    for (Hire hire: hires){
                        if (carIds.contains(hire.getCarId())){
                            hiredIds.add(hire.getCarId());
                        }
                    }
                    listener.onRideChanged(hiredIds);
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
        void onRideChanged(List<String> hires);
    }
}
