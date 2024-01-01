package com.sanny_tech.carapp.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.MainActivity;
import com.sanny_tech.carapp.entities.CarBookRequest;
import com.sanny_tech.carapp.storage.RemoteMessageSaver;
import com.sanny_tech.carapp.taxi_utils.ClientRequest;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "abiri_message";
    private static final int NOTIFICATION_ID = 123;

    @Override
    public void onNewToken(@NonNull String token) {
        FCMTokenManager.saveToken(getApplicationContext(), token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        createNotificationChannel();
        long id;
        Log.e("Messaging", "isReceived");
        try {
            id = saveMessage(remoteMessage);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        if (remoteMessage.getData().size() > 0) {
            Log.e("Messaging", String.valueOf(remoteMessage.getData()));

            RemoteMessage.Notification notification = remoteMessage.getNotification();

            Map<String, String> data = remoteMessage.getData();

            assert notification != null;
            String content = notification.getBody();

            String title = notification.getTitle();
            // Handle the data payload and notification payload based on your requirements
            try {
                showNotification(content, processData(data),title,remoteMessage,id);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        super.onMessageReceived(remoteMessage);
    }

    private Object processData(Map<String, String> data) {
        if (data.containsKey("ride_id")) {
            String ride_id = data.get("ride_id");
            String user_name = data.get("user_name");
            String user_phone = data.get("user_phone");
            String client_id = data.get("client_id");
            float dest_lat = Float.parseFloat(data.get("dest_lat"));
            float dest_lon = Float.parseFloat(data.get("dest_lon"));
            float current_lat = Float.parseFloat(data.get("current_lat"));
            float current_lon = Float.parseFloat(data.get("current_lon"));

            ClientRequest request = new ClientRequest();
            request.setRide_id(ride_id);
            request.setClient_id(client_id);
            request.setUser_name(user_name);
            request.setUser_phone(user_phone);
            request.setCurrent_lat(current_lat);
            request.setCurrent_lon(current_lon);
            request.setDest_lat(dest_lat);
            request.setDest_lon(dest_lon);
            return request;
        }else if (data.containsKey("booking_id")){
            String booking_id = data.get("booking_id");
            String user_name = data.get("user_name");
            String car_id = data.get("car_id");
            String user_phone = data.get("user_phone");
            String client_id = data.get("client_id");
            CarBookRequest request = new CarBookRequest();
            request.setClient_id(client_id);
            request.setUser_name(user_name);
            request.setUser_phone(user_phone);
            request.setCar_id(car_id);
            request.setBooking_id(booking_id);
            return request;
        }
        return null;
    }

    private long saveMessage(RemoteMessage remoteMessage) throws JSONException {
        long id = RemoteMessageSaver.saveRemoteMessage(getApplicationContext(),remoteMessage);
        Log.e("Messaging", "message saved");
        return id;
    }

    private void showNotification(String content,
                                  Object object, String title, RemoteMessage remoteMessage, long id) throws JSONException {
        // Create a notification channel (for Android 8.0 and above)
        Log.e("Messaging", "notification shown");

        // Load the large icon and image using Glide
        Bitmap largeIconBitmap = null;
            try {
                largeIconBitmap = Glide.with(this)
                        .asBitmap()
                        .load(R.mipmap.ic_abiri2)
                        .submit()
                        .get();
            } catch (Exception e) {
                e.printStackTrace();
            }

        // Create the notification intent
        Intent intent = new Intent(this, MainActivity.class);
        if (object instanceof ClientRequest) {
            ClientRequest clientRequest1 = (ClientRequest) object;
            intent.putExtra("request", clientRequest1);
            intent.putExtra("id", id);
        } else if (object instanceof CarBookRequest){
            CarBookRequest carBookRequest = (CarBookRequest) object;
            // Initialize anotherObject
            intent.putExtra("request", carBookRequest);
            intent.putExtra("id", id);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent acceptIntent = new Intent(this, MainActivity.class);
        if (object instanceof ClientRequest) {
            ClientRequest clientRequest1 = (ClientRequest) object;
            acceptIntent.putExtra("request", (Parcelable) clientRequest1);
            acceptIntent.putExtra("id", id);
        } else if (object instanceof CarBookRequest){
            CarBookRequest carBookRequest = (CarBookRequest) object;
            // Initialize anotherObject
            acceptIntent.putExtra("request", (Parcelable) carBookRequest);
            acceptIntent.putExtra("id", id);
        }
        acceptIntent.setAction("ACCEPT_ACTION");
// Add any necessary extras to the acceptIntent if required
        PendingIntent acceptPendingIntent = PendingIntent.getActivity(this,
                0, acceptIntent,  PendingIntent.FLAG_IMMUTABLE);

        Intent declineIntent = new Intent(this, MainActivity.class);
        if (object instanceof ClientRequest) {
            ClientRequest clientRequest1 = (ClientRequest) object;
            declineIntent.putExtra("request", (Parcelable) clientRequest1);
            declineIntent.putExtra("id", id);
        } else if (object instanceof CarBookRequest){
            CarBookRequest carBookRequest = (CarBookRequest) object;
            // Initialize anotherObject
            declineIntent.putExtra("request", (Parcelable) carBookRequest);
            declineIntent.putExtra("id", id);
        }
        declineIntent.setAction("DECLINE_ACTION");
// Add any necessary extras to the declineIntent if required
        PendingIntent declinePendingIntent = PendingIntent.getActivity(this, 0,
                declineIntent,  PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_abiri_foreground)
                .setLargeIcon(largeIconBitmap)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                // Add accept and decline actions
                .addAction(R.drawable.baseline_check_24, "Accept", acceptPendingIntent)
                .addAction(R.drawable.baseline_error_outline_24, "Decline", declinePendingIntent);


        // Display the notification
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "My Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Description");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

