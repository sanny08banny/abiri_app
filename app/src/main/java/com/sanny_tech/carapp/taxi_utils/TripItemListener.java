package com.sanny_tech.carapp.taxi_utils;

import static android.content.Context.MODE_PRIVATE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.sanny_tech.carapp.R;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class TripItemListener {

    private static final String TAG = "TripItemListener";
    private static final String CHANNEL_ID = "TripItemChannel";
    private static final int NOTIFICATION_ID = 123;
    private FirebaseFirestore firestore;
    private ListenerRegistration listenerRegistration;
    private Context context;
    private NotificationManagerCompat notificationManager;
    private TripItemListenerCallback callback;

    public TripItemListener(Context context, TripItemListenerCallback callback) {
        this.callback = callback;
        this.context = context;
        firestore = FirebaseFirestore.getInstance();
        notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannel(context);
    }

    public void startListening() {
        firestore.collection("trips")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed", e);
                        return;
                    }
                    if (snapshots != null) {
                        for (DocumentChange change : snapshots.getDocumentChanges()) {
                            if (change.getType() == DocumentChange.Type.ADDED) {
                                Trip tripItem = change.getDocument().toObject(Trip.class);
                                // Notify the callback about the new addition
                                if (tripItem.getUser_id().matches(getCurrentAccountId())) {
                                    callback.onNewTripItem(tripItem);
                                    // Show local notification
                                    showNotification(tripItem);
                                }
                            }
                        }
                    }
                });
    }

    public void stopListening() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "TripItemChannel";
            String description = "Channel for new trip items";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(Trip tripItem) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_abiri_foreground)
                .setContentTitle("Trip completed!")
                .setContentText("Your trip to " + tripItem.getDest() + " has been completed.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // Callback interface to notify about new trip items
    public interface TripItemListenerCallback {
        void onNewTripItem(Trip tripItem);
    }
    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
}

