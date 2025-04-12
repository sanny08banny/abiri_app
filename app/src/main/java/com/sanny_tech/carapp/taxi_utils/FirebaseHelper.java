package com.sanny_tech.carapp.taxi_utils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseHelper {

    public interface MapKeyCallback {
        void onMapKeyReceived(String mapKey);
        void onError(DatabaseError error);
    }

    private MapKeyCallback callback;

    public FirebaseHelper(MapKeyCallback callback) {
        this.callback = callback;
    }

    public void fetchMapKey() {
        DatabaseReference hireListener = FirebaseDatabase.getInstance().getReference("configurations");
        hireListener.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String mapKey = null;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String key = snapshot.getKey();
                        if ("maps_key".equals(key)) {
                            mapKey = snapshot.getValue(String.class);
                            break;  // Found the mapKey, no need to continue loop
                        }
                    }
                    if (callback != null) {
                        callback.onMapKeyReceived(mapKey);
                    }
                } else {
                    if (callback != null) {
                        callback.onMapKeyReceived(null);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (callback != null) {
                    callback.onError(databaseError);
                }
            }
        });
    }
}

