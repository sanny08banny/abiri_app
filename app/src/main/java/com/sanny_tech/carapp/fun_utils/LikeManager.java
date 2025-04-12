package com.sanny_tech.carapp.fun_utils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LikeManager {

    private DatabaseReference adminsRef;

    public LikeManager() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        adminsRef = database.getReference("likes");
    }

    // Method to request admin access
    public void likeFunItem(String userId, String id, boolean status) {
        adminsRef.child(userId + "_" + id).setValue(
                status
        );
    }

    // Method to check if a user is an admin
    public void checkLikeStatus(String userId, String id, final LikeStatusCallback callback) {
        adminsRef.child(userId + "_" + id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean isLiked = Boolean.TRUE.equals(dataSnapshot.getValue(boolean.class));
                    callback.onLikeStatusChecked(isLiked);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
                callback.onLikeStatusChecked(false);
            }
        });
    }

    // Callback interface for admin status check
    public interface LikeStatusCallback {
        void onLikeStatusChecked(boolean isAdmin);
    }
}

