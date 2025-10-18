package com.sanny_tech.carapp.utils;

import android.util.Log;


import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.clientlib.NimbusReconnectWorker;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessaging";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ---- Raw message dump ----
        Log.d(TAG, "================= FCM onMessageReceived =================");
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Message ID: " + remoteMessage.getMessageId());
        Log.d(TAG, "Sent time: " + remoteMessage.getSentTime());
        Log.d(TAG, "Collapse Key: " + remoteMessage.getCollapseKey());
        Log.d(TAG, "Priority: " + remoteMessage.getPriority());

        // Notification payload (if present)
        RemoteMessage.Notification n = remoteMessage.getNotification();
        if (n != null) {
            Log.d(TAG, "-- Notification --");
            Log.d(TAG, "  Title: " + n.getTitle());
            Log.d(TAG, "  Body : " + n.getBody());
            Log.d(TAG, "  Image: " + n.getImageUrl());
            Log.d(TAG, "  ChannelId: " + n.getChannelId());
        } else {
            Log.d(TAG, "No notification payload.");
        }

        // Data payload
        if (!remoteMessage.getData().isEmpty()) {
            Log.d(TAG, "-- Data --");
            for (Map.Entry<String,String> e : remoteMessage.getData().entrySet()) {
                Log.d(TAG, "  " + e.getKey() + " = " + e.getValue());
            }
        } else {
            Log.d(TAG, "No data payload.");
        }

        // ---- Decision logic ----
        boolean shouldSync = false;

        if (n != null) {
            String title = n.getTitle();
            Log.d(TAG, "Checking notification title: " + title);
            if ("Insider".equalsIgnoreCase(title)) {
                Log.d(TAG, "Title matched 'Insider' -> shouldSync = true");
                shouldSync = true;
            }
        }

        if (!remoteMessage.getData().isEmpty()) {
            String action = remoteMessage.getData().get("msg");
            Log.d(TAG, "Checking data action: " + action);
            if ("sync".equalsIgnoreCase(action)) {
                Log.d(TAG, "Action matched 'sync' -> shouldSync = true");
                shouldSync = true;
            }
        }

        Log.d(TAG, "Final shouldSync = " + shouldSync);

//        if (shouldSync) {
//            Log.w(TAG, "Starting WebSocketService");
//            String sessionId = SessionIdManager.getSessionId(this);
//            Log.d(TAG, "Retrieved sessionId: " + sessionId);
//            if (sessionId != null && !sessionId.isEmpty()) {
//                WebSocketService.ensureRunning(this, sessionId, true);
//                Log.d(TAG, "Called WebSocketService.ensureRunning");
//            } else {
//                Log.w(TAG, "No session_id available, cannot start WebSocketService");
//            }
//        }
        if (shouldSync) {
            Log.i(TAG, "Triggering NimbusReconnectWorker from FCM push");

            try {
                OneTimeWorkRequest workRequest =
                        new OneTimeWorkRequest.Builder(NimbusReconnectWorker.class)
                                .setConstraints(
                                        new Constraints.Builder()
                                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                                .build()
                                )
                                .build();

                WorkManager.getInstance(getApplicationContext())
                        .enqueueUniqueWork(
                                "nimbus_reconnect_worker_fcm",
                                ExistingWorkPolicy.REPLACE,
                                workRequest
                        );
            } catch (Exception e) {
                Log.e(TAG, "Failed to enqueue reconnect worker", e);
            }
        }


        Log.d(TAG, "================= End onMessageReceived =================");
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "FCM Token refreshed: " + token);
        // send token to your server here
    }
}
