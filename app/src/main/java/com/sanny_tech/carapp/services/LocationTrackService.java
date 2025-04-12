package com.sanny_tech.carapp.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.common.net.InternetDomainName;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.AdminActivity;
import com.sanny_tech.carapp.activities.MainActivity;
import com.sanny_tech.carapp.entities.Ride;
import com.sanny_tech.carapp.entities.TaxiLocation;
import com.sanny_tech.carapp.taxi_utils.TaxisAvailable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class LocationTrackService extends Service {

    private static final int NOTIFICATION_ID = 123;
    private static final String CHANNEL_ID = "LocationTrackServiceChannel";

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private DatabaseReference locationReference;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationReference = FirebaseDatabase.getInstance().getReference().child("user_location");
        createNotificationChannel();
        // Start the foreground service with a basic notification
        startForeground(NOTIFICATION_ID, createBasicNotification());
        // Load the notification icon asynchronously
        new LoadNotificationIconTask(this).execute();
        createLocationRequest();
        startLocationUpdates();
    }

    private Notification createBasicNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("Tracking your live location.")
                .setSmallIcon(R.drawable.ic_abiri_foreground)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        return builder.build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static class LoadNotificationIconTask extends AsyncTask<Void, Void, Bitmap> {
        private final WeakReference<LocationTrackService> serviceReference;

        LoadNotificationIconTask(LocationTrackService service) {
            serviceReference = new WeakReference<>(service);
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            LocationTrackService service = serviceReference.get();
            if (service != null) {
                try {
                    // Load the large icon using Glide
                    return Glide.with(service)
                            .asBitmap()
                            .load(R.drawable.carpublictransporttaxivehicleicon) // Replace with your actual drawable resource
                            .submit()
                            .get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            LocationTrackService service = serviceReference.get();
            if (service != null && bitmap != null) {
                service.startForeground(NOTIFICATION_ID, service.buildNotification(bitmap));
            }
        }
    }


    private Notification buildNotification(Bitmap largeIcon) {
        // Create an intent that will open your main activity when the notification is tapped
        Intent notificationIntent = new Intent(this, AdminActivity.class);
        notificationIntent.putExtra("fragment", "driver");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Define the custom sound URI
        Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.car_start_ignition);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("Tracking your live location. Make your taxi unavailable to switch off")
                .setColor(getResources().getColor(R.color.black))
                .setSmallIcon(R.drawable.ic_abiri_foreground)
                .setLargeIcon(largeIcon)
                .setContentIntent(pendingIntent)
                .setSound(soundUri)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder.build();
    }
    private void createNotificationChannel() {
        Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.engine_car_start);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            serviceChannel.setSound(soundUri,attributes);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }


    private void createLocationRequest() {
        locationRequest = LocationRequest
                .create()
        .setInterval(10000) // Update interval in milliseconds
        .setFastestInterval(5000) // Fastest update interval
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateLocationInFirebase(location.getLatitude(),location.getLongitude());
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void updateFirebaseWithLocation(Location location) {
        locationReference.setValue(location); // You may want to structure the data differently
    }
    private void updateLocationInFirebase(double latitude, double longitude) {
        // Store the driver's current location in Firebase Realtime Database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("taxi_locations");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TaxiLocation taxiLocation = snapshot.getValue(TaxiLocation.class);
                    if (taxiLocation != null && taxiLocation.getDriverId().equals(getCurrentAccountId())) {
                        taxiLocation.setLatitude((float) latitude);
                        taxiLocation.setLongitude((float) longitude);
                        reference.child(taxiLocation.getDriverId()).setValue(taxiLocation);
                        TaxisAvailable available = taxiLocation.createTaxiAvailble();
                        String category = taxiLocation.getTaxiInit().getCategory();
                        if (category.equals("Boda Boda")){
                            category = "BodaBoda";
                        }
                        DatabaseReference availableRef = FirebaseDatabase.getInstance().getReference("taxis");
                        availableRef.child("available")
                                .child(category)
                                .child(taxiLocation.getTaxiInit().getTaxi_id())
                                .setValue(available);
                    }
                }
                // You can pass this list to your UI or perform further operations
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
    }
    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        // Stop location updates when the service is destroyed
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}