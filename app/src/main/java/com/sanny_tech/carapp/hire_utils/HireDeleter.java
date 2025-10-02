package com.sanny_tech.carapp.hire_utils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HireDeleter {

    private DatabaseReference databaseReference;

    public HireDeleter() {
        // Initialize the reference to the "hires" node in your Firebase Database
        this.databaseReference = FirebaseDatabase.getInstance().getReference("hires");
    }

    public void deleteDeclinedHires() {
        // Add a listener to query all hires
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Hire hire = snapshot.getValue(Hire.class);
                        if (hire != null && "declined".equals(hire.getStatus())) {
                            // Delete the declined hire
                            snapshot.getRef().removeValue().addOnSuccessListener(aVoid -> {
                                // Successfully deleted the hire
                                System.out.println("Hire with ID " + snapshot.getKey() + " has been deleted.");
                            }).addOnFailureListener(e -> {
                                // Failed to delete the hire
                                System.err.println("Failed to delete hire with ID " + snapshot.getKey() + ": " + e.getMessage());
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle potential errors
                System.err.println("Error fetching data: " + databaseError.getMessage());
            }
        });
    }
}
