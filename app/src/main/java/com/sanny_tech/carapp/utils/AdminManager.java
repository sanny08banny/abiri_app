package com.sanny_tech.carapp.utils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminManager {

    private DatabaseReference adminsRef;

    public AdminManager() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        adminsRef = database.getReference("admins");
    }

    // Method to request admin access
    public void requestAdminAccess(String userId) {
        adminsRef.child(userId).setValue(true);
    }

    // Method to check if a user is an admin
    public void getAdminAccess(String userId, final AdminStatusCallback callback) {
        adminsRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isAdmin = dataSnapshot.exists();
                callback.onAdminStatusChecked(isAdmin);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
                callback.onAdminStatusChecked(false);
            }
        });
    }

    // Callback interface for admin status check
    public interface AdminStatusCallback {
        void onAdminStatusChecked(boolean isAdmin);
    }
}

