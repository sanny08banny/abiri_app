package com.sanny_tech.carapp.utils;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseError;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.NotificationsActivity;
import com.sanny_tech.carapp.activities.SplashActivity;
import com.sanny_tech.carapp.activities.TaxiMapsActivity;
import com.sanny_tech.carapp.bubbles.BubbleActivity;
import com.sanny_tech.carapp.entities.CarBookRequest;
import com.sanny_tech.carapp.hire_utils.HireDeleter;
import com.sanny_tech.carapp.storage.RemoteMessageSaver;
import com.sanny_tech.carapp.taxi_utils.ClientRequest;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sanny_tech.carapp.taxi_utils.FirebaseHelper;

import org.json.JSONException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MyFirebaseMessagingServiceOld extends FirebaseMessagingService {
    private static final String CHANNEL_ID_WITH_ACTIONS = "abiri_message_with_actions";
    private static final String CHANNEL_ID_GENERAL = "abiri_message_general";
    private static final int NOTIFICATION_ID = (int) System.currentTimeMillis();
    private String content;
    private String title;

    @Override
    public void onNewToken(@NonNull String token) {
//        FCMTokenManager.saveToken(getApplicationContext(), token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        createNotificationChannels();
        long id = 0;
        Log.e("Messaging", "isReceived");
        if (!remoteMessage.getData().isEmpty()) {
            Log.e("Messaging", String.valueOf(remoteMessage.getData()));

            Map<String, String> data = remoteMessage.getData();
            boolean showActions = shouldShowActions(data);

            if (data.containsKey("booking_id")) {
                content = data.get("user_name") + " has requested for a car hire.";
                title = "New booking request";
            } else if (data.containsKey("ride_id")) {
                content = data.get("user_name") + " has requested for a ride.";
                title = "Taxi request";
            } else if (data.containsKey("Rejected")) {
                HireDeleter hireDeleter = new HireDeleter();
                hireDeleter.deleteDeclinedHires();
                content = "Your request has been declined";
                title = "Abiri Africa";
            } else {
                content = "Your request has been accepted.";
                title = "We found you!";
            }
            if (remoteMessage.getNotification() != null){
                if (remoteMessage.getNotification().getTitle() != null) {
                    Log.d("title", remoteMessage.getNotification().getTitle());
                }
                if (remoteMessage.getNotification().getBody() != null) {
                    Log.d("nbody", remoteMessage.getNotification().getBody());
                }
            }

            if (showActions) {
                updateWidget(title);
                try {
                    id = saveMessage(remoteMessage);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                showNotification(content, processData(data), title,
                        remoteMessage, id, showActions);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        super.onMessageReceived(remoteMessage);
    }

    private boolean shouldShowActions(Map<String, String> data) {
        return data.containsKey("ride_id") || data.containsKey("booking_id");
    }

    private Object processData(Map<String, String> data) {
        if (data.containsKey("ride_id")) {
            String rideId = data.get("ride_id");
            String senderId = data.get("sender_id");
            double price = Double.parseDouble(data.get("price"));
            String userName = data.get("user_name");
            double currentLat = Double.parseDouble(data.get("current_lat"));
            double currentLon = Double.parseDouble(data.get("current_lon"));
            double destLat = Double.parseDouble(data.get("dest_lat"));
            double destLon = Double.parseDouble(data.get("dest_lon"));
            String userPhone = data.get("user_phone");
            String destName = data.get("dest_name");

            ClientRequest clientRequest = new ClientRequest(senderId, price, userName, currentLat,
                    currentLon, destLat, destLon, rideId, userPhone, destName);

            return clientRequest;
        } else if (data.containsKey("booking_id")) {
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
        long id = RemoteMessageSaver.saveRemoteMessage(getApplicationContext(), remoteMessage.getData());
        Log.e("Messaging", "message saved");
        return id;
    }

    private void showNotification(String content,
                                  Object object, String title, RemoteMessage remoteMessage, long id, boolean showActions) throws JSONException {
        Log.e("Messaging", "notification shown");

        Bitmap largeIconBitmap = null;
        try {
            largeIconBitmap = Glide.with(this)
                    .asBitmap()
                    .load(R.mipmap.ic_abiri_a)
                    .submit()
                    .get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, SplashActivity.class);
        if (object instanceof ClientRequest) {
            ClientRequest clientRequest1 = (ClientRequest) object;
            intent.putExtra("request", clientRequest1);
            intent.putExtra("id", id);
        } else if (object instanceof CarBookRequest) {
            CarBookRequest carBookRequest = (CarBookRequest) object;
            intent.putExtra("request", carBookRequest);
            intent.putExtra("id", id);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder;
        if (showActions) {
            notificationBuilder = getNotificationBuilderWithActionsAndBubble(
                    title, content, largeIconBitmap, pendingIntent, object, id, intent);
        } else {
            notificationBuilder = getGeneralNotificationBuilder(title, content, largeIconBitmap, pendingIntent);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        if (object instanceof ClientRequest) {
            FirebaseHelper firebaseHelper = new FirebaseHelper(new FirebaseHelper.MapKeyCallback() {
                @Override
                public void onMapKeyReceived(String mapKey) {
                    if (mapKey != null) {
                        Intent notificationIntent = new Intent(getApplicationContext(),
                                TaxiMapsActivity.class);
                        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        notificationIntent.putExtra("request", (ClientRequest) object);
                        notificationIntent.putExtra("key", mapKey);
                        try {
                            if (id != 0) {
                                RemoteMessageSaver.readMessageById(getApplicationContext(), id);
                            }
                        } catch (JSONException e) {
                            Log.e("Main activity", String.valueOf(e));
                        }
                        Intent localIntent = new Intent("TripRadarNotification");
                        localIntent.putExtra("request", (ClientRequest) object);
                        localIntent.putExtra("key", mapKey);
                        if (isAppInForeground() &&
                                isActivityInForeground(TaxiMapsActivity.class.getName())) {
                            Intent nIntent = new Intent(getApplicationContext(),
                                    NotificationsActivity.class);
                            nIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(nIntent);
                        }else {
                            startActivity(notificationIntent);
                        }
                    } else {
                        // Handle case when mapKey is null
                    }
                }

                @Override
                public void onError(DatabaseError error) {
                    // Handle errors
                }
            });

            firebaseHelper.fetchMapKey();
        }
    }

    private NotificationCompat.Builder getNotificationBuilderWithActionsAndBubble(
            String title, String content, Bitmap largeIconBitmap, PendingIntent pendingIntent,
            Object object, long id, Intent intent) {
        Intent acceptIntent = new Intent(this, SplashActivity.class);
        if (object instanceof ClientRequest) {
            ClientRequest clientRequest1 = (ClientRequest) object;
            acceptIntent.putExtra("request", (Parcelable) clientRequest1);
            acceptIntent.putExtra("id", id);
        } else if (object instanceof CarBookRequest) {
            CarBookRequest carBookRequest = (CarBookRequest) object;
            acceptIntent.putExtra("request", (Parcelable) carBookRequest);
            acceptIntent.putExtra("id", id);
        }
        acceptIntent.setAction("ACCEPT_ACTION");
        PendingIntent acceptPendingIntent = PendingIntent.getActivity(this, 0, acceptIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent declineIntent = new Intent(this, SplashActivity.class);
        if (object instanceof ClientRequest) {
            ClientRequest clientRequest1 = (ClientRequest) object;
            declineIntent.putExtra("request", (Parcelable) clientRequest1);
            declineIntent.putExtra("id", id);
        } else if (object instanceof CarBookRequest) {
            CarBookRequest carBookRequest = (CarBookRequest) object;
            declineIntent.putExtra("request", (Parcelable) carBookRequest);
            declineIntent.putExtra("id", id);
        }
        declineIntent.setAction("DECLINE_ACTION");
        PendingIntent declinePendingIntent = PendingIntent.getActivity(this, 0, declineIntent, PendingIntent.FLAG_IMMUTABLE);

        Uri soundUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.sms_engine);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_WITH_ACTIONS)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_abiri_b_foreground)
                .setLargeIcon(largeIconBitmap)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setSound(soundUri)
                .addAction(R.drawable.baseline_check_24, "Accept", acceptPendingIntent)
                .addAction(R.drawable.baseline_error_outline_24, "Decline", declinePendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Uri iconUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName()
                    + "/" + R.drawable.abiri_title);
            IconCompat bubbleIcon = IconCompat.createWithContentUri(iconUri);

            // Create the Person object
            Person person = new Person.Builder()
                    .setName(title) // Replace with the actual name
                    .setImportant(true)
                    .build();

            // Create messaging style
            NotificationCompat.MessagingStyle messagingStyle =
                    new NotificationCompat.MessagingStyle(person)
                            .setConversationTitle(title)
                            .addMessage(new NotificationCompat.MessagingStyle.Message(content,
                                    System.currentTimeMillis(), person));

            // Create shortcut
            String shortcutId = "chat_shortcut";
            ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(getApplicationContext(), shortcutId)
                    .setCategories(Collections.singleton("com.example.category.IMG_SHARE_TARGET"))
                    .setIntent(new Intent(Intent.ACTION_VIEW, null, getApplicationContext(), BubbleActivity.class))
                    .setLongLived(true)
                    .setShortLabel(title)
                    .setIcon(bubbleIcon)
                    .build();
            ShortcutManagerCompat.pushDynamicShortcut(getApplicationContext(), shortcut);
            PendingIntent pendingIntentB = PendingIntent.getActivity(
                    this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

            NotificationCompat.BubbleMetadata bubbleMetadata = new
                    NotificationCompat.BubbleMetadata.Builder()
                    .setDesiredHeight(600)
                    .setIcon(bubbleIcon)
                    .setIntent(pendingIntentB)
                    .build();
            builder.setBubbleMetadata(bubbleMetadata);
//                    .addPerson(person)
//                    .setWhen(System.currentTimeMillis())
//                    .setStyle(messagingStyle)
//                    .setShortcutInfo(shortcut);
        }

        return builder;
    }

    private NotificationCompat.Builder getGeneralNotificationBuilder(String title, String content, Bitmap largeIconBitmap, PendingIntent pendingIntent) {
        Uri soundUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.sms_car);

        return new NotificationCompat.Builder(this, CHANNEL_ID_GENERAL)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_abiri_b_foreground)
                .setLargeIcon(largeIconBitmap)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setSound(soundUri);
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri soundUri = Uri.parse("android.resource://" +
                    getApplicationContext().getPackageName() + "/" + R.raw.sms_engine);
            Uri soundUri1 = Uri.parse("android.resource://" +
                    getApplicationContext().getPackageName() + "/" + R.raw.sms_car);

            NotificationChannel channelWithActions = new NotificationChannel(
                    CHANNEL_ID_WITH_ACTIONS,
                    "Notifications with Actions",
                    NotificationManager.IMPORTANCE_HIGH);
            channelWithActions.setDescription("Channel for notifications with accept and decline actions");
            channelWithActions.setSound(soundUri, null);

            NotificationChannel channelGeneral = new NotificationChannel(
                    CHANNEL_ID_GENERAL,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            channelGeneral.setDescription("General notifications without actions");
            channelGeneral.setSound(soundUri1, null);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channelWithActions);
            notificationManager.createNotificationChannel(channelGeneral);
        }
    }

    private void updateWidget(String message) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_layout);

        // Update the widget's TextView
        views.setTextViewText(R.id.widget_text, message);

        // Get all widget instances
        ComponentName widget = new ComponentName(this, MyAppWidgetProvider.class);
        appWidgetManager.updateAppWidget(widget, views);
    }
    private boolean isAppInForeground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isActivityInForeground(String activityClassName) {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
        if (taskInfo != null && !taskInfo.isEmpty()) {
            String currentActivity = taskInfo.get(0).topActivity.getClassName();
            return currentActivity.equals(activityClassName);
        }
        return false;
    }
}

