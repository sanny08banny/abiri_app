package com.sanny_tech.carapp.services;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import com.google.common.net.InternetDomainName;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sanny_tech.carapp.MyApplication;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.MainActivity;
import com.sanny_tech.carapp.activities.TaxiMapsActivity;
import com.sanny_tech.carapp.databinding.OverlayLayoutBinding;
import com.sanny_tech.carapp.entities.Ride;
import com.sanny_tech.carapp.entities.TaxiLocation;
import com.sanny_tech.carapp.taxi_utils.OrientationManager;
import com.sanny_tech.carapp.taxi_utils.RouteCalculator;
import com.sanny_tech.carapp.taxi_utils.TaxisAvailable;
import com.sanny_tech.carapp.taxi_utils.Trip;
import com.sanny_tech.carapp.taxi_utils.TripActivity;
import com.sanny_tech.carapp.utils.JourneyStatusManager;
import com.sanny_tech.carapp.utils.RequestManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class FloatingOverlayService extends Service implements MyApplication.OnAppBackgroundListener {

    private static final String CHANNEL_ID = "FloatingOverlayServiceChannel";
    private static final int NOTIFICATION_ID = 45;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private OverlayLayoutBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng pickupLatLng;
    private LatLng destinationLatLng;
    private static final float ARRIVAL_RADIUS_METERS = 50.0f; // Define your arrival radius
    private boolean isDetailsShown = false;
    private ValueAnimator borderAnimator;
    private Geocoder geocoder;
    private JourneyStatusManager journeyStatusManager;
    private DatabaseReference taxiReference;
    private CountDownTimer countDownTimer;
    private boolean arrivedPickUp = false;
    private DatabaseReference availableRef;
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference("taxi_rides");


    @Override
    public void onCreate() {
        super.onCreate();
        MyApplication.getInstance().setOnAppBackgroundListener(this);

        // Initialize WindowManager
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Inflate the view using View Binding
        Context themedContext = new ContextThemeWrapper(getApplicationContext(), R.style.Base_Theme_CarApp);
        LayoutInflater inflater = LayoutInflater.from(themedContext);
        binding = OverlayLayoutBinding.inflate(inflater);
        View floatingView = binding.getRoot();

        geocoder = new Geocoder(this, Locale.getDefault());
        journeyStatusManager = new JourneyStatusManager(getApplicationContext());
        taxiReference = FirebaseDatabase.getInstance().getReference("taxi_locations");
        availableRef = FirebaseDatabase.getInstance().getReference("taxis");
        // Set up layout parameters
        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        layoutParams.gravity = Gravity.TOP | Gravity.START;
        // Add the view to the window
        windowManager.addView(floatingView, layoutParams);
        binding.getRoot().setVisibility(View.GONE); // Initially hide the overlay

        // Set up button click listener
        binding.actionButton.setOnClickListener(v -> {
            if ("Start Trip".equals(binding.actionButton.getText().toString())) {
                if (countDownTimer != null) {
                    binding.countdownTextView.setVisibility(View.GONE);
                    countDownTimer.cancel();
                }
                if (!journeyStatusManager.isJourneyStarted()) {
                    journeyStatusManager.setJourneyStarted(true);
                }
                hideDetailsLayout();
                if (destinationLatLng != null) {
                    startTrip(true);
                    openGoogleMaps(getApplicationContext(), destinationLatLng);
                    if (countDownTimer != null){
                        countDownTimer.cancel();
                        binding.countdownTextView.setVisibility(View.GONE);
                    }
                }
                // Handle start trip logic
                binding.statusTextView.setText("On the way...");
                binding.actionButton.setText("End Trip");
            } else if ("Arrived".equals(binding.actionButton.getText().toString())) {
                startDriverSearchTimer();
                notifyClient();
                arrivedPickUp = true;
                binding.statusTextView.setText("Waiting...");
                binding.actionButton.setText("Start Trip");
                showDetailsLayout();
            } else {
                // Handle end trip logic
                startTrip(false);
                cancelRide();
                stopSelf();
            }
        });

        // Start the pulsing animation
        startBorderAnimation();
    }

    private void notifyClient() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("taxi_rides");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot dataSnapshot) {
                List<Ride> rides = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ride ride = snapshot.getValue(Ride.class);
                    if (ride != null && ride.getDriver_id().equals(getCurrentAccountId())) {
                        ride.setStatus("waiting");
                        reference.child(getCurrentAccountId()).setValue(ride);
                    }
                }
                // You can pass this list to your UI or perform further operations

            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
    }
    private void startTrip(boolean isStarted) {
        DatabaseReference tripStartReference = FirebaseDatabase.getInstance().getReference("taxi_rides");
        tripStartReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot dataSnapshot) {
                List<Ride> rides = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ride ride = snapshot.getValue(Ride.class);
                    if (ride != null && ride.getDriver_id().equals(getCurrentAccountId())) {
                        if (isStarted) {
                            ride.setStatus("started");
                            ride.setStart_time(String.valueOf(System.currentTimeMillis()));
                        }else {
                            ride.setStatus("completed");
                        }
                        tripStartReference.child(getCurrentAccountId()).setValue(ride);
                    }
                }
                // You can pass this list to your UI or perform further operations

            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Get pickup and destination coordinates from the intent
        double pickupLat = intent.getDoubleExtra("pickup_lat", 0);
        double pickupLng = intent.getDoubleExtra("pickup_lng", 0);
        double destLat = intent.getDoubleExtra("dest_lat", 0);
        double destLng = intent.getDoubleExtra("dest_lng", 0);

        pickupLatLng = new LatLng(pickupLat, pickupLng);
        destinationLatLng = new LatLng(destLat, destLng);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Start tracking location
        startLocationUpdates();

        // Create and display notification
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, getNotification(
                "Trip manager."));
        getLocationName(pickupLatLng.latitude, pickupLatLng.longitude, "pickup");
        getLocationName(destinationLatLng.latitude, destinationLatLng.longitude, "destination");


        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (binding.getRoot() != null) {
            windowManager.removeView(binding.getRoot());
        }
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (borderAnimator != null && borderAnimator.isRunning()) {
            borderAnimator.cancel();
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // 5 seconds interval
        locationRequest.setFastestInterval(2000); // 2 seconds fastest interval
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
                Looper.getMainLooper());
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                updateOverlay(location);
                updateLocationInFirebase(latitude, longitude);
            }
        }
    };

    private void updateOverlay(Location location) {
        float[] results = new float[1];

        // Check distance to pickup location
        Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                pickupLatLng.latitude, pickupLatLng.longitude, results);
        float distanceToPickup = results[0];

        // Check distance to destination location
        Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                destinationLatLng.latitude, destinationLatLng.longitude, results);
        float distanceToDestination = results[0];

        if (distanceToPickup <= ARRIVAL_RADIUS_METERS) {
            if (!arrivedPickUp && !journeyStatusManager.isJourneyStarted()) {
                binding.statusTextView.setText("Arriving at Pickup");
                binding.actionButton.setText("Arrived");
                showDetailsLayout();
            }else {
                if (journeyStatusManager.isJourneyStarted()) {
                    hideDetailsLayout();
                }
            }
        } else if (distanceToDestination <= ARRIVAL_RADIUS_METERS) {
            showDetailsLayout();
            binding.statusTextView.setText("Arriving at Destination");
            binding.actionButton.setText("End Trip");
        } else {
            hideDetailsLayout();
            binding.statusTextView.setText("On the way...");
        }
    }

    private void startBorderAnimation() {
        borderAnimator = ObjectAnimator.ofFloat(
                binding.logoImageView, "alpha", 1f, 1.2f);
        borderAnimator.setRepeatMode(ValueAnimator.REVERSE);
        borderAnimator.setRepeatCount(ValueAnimator.INFINITE);
        borderAnimator.setDuration(1000);
        borderAnimator.start();
    }

    private void showDetailsLayout() {
        if (!isDetailsShown) {
            binding.detailsLayout.setVisibility(View.VISIBLE);
            isDetailsShown = true;
        }
    }

    private void hideDetailsLayout() {
        if (isDetailsShown) {
            binding.detailsLayout.setVisibility(View.GONE);
            isDetailsShown = false;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Floating Overlay Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private NotificationCompat.Builder getNotificationBuilder(String contentText) {
        Intent notificationIntent = new Intent(this, MainActivity.class); // Update with your main activity
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Abiri")
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_abiri_a_foreground) // Update with your notification icon
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
    }

    private Notification getNotification(String contentText) {
        return getNotificationBuilder(contentText).build();
    }

    private void updateLocationInFirebase(double latitude, double longitude) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ride ride = snapshot.getValue(Ride.class);
                    if (ride != null && ride.getDriver_id().equals(getCurrentAccountId())) {
                        ride.setDriver_lat((float) latitude);
                        ride.setDriver_lon((float) longitude);
                        reference.child(ride.getDriver_id()).setValue(ride);
                    }
                }
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
    public void onAppBackground() {
        if (binding != null && binding.getRoot() != null) {
            binding.getRoot().setVisibility(View.VISIBLE); // Show the overlay
        }
    }

    @Override
    public void onAppForeground() {
        if (binding != null && binding.getRoot() != null) {
            binding.getRoot().setVisibility(View.GONE); // Hide the overlay
        }
    }

    private void getLocationName(double latitude, double longitude, String type) {
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String locationName = address.getAddressLine(0);
                    if ("pickup".equals(type)) {
                        runOnUiThread(() -> binding.title.setText(locationName));
                    } else if ("destination".equals(type)) {
                        runOnUiThread(() -> binding.title2.setText(locationName));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void runOnUiThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    private void openGoogleMaps(Context context, LatLng dest) {
        String destLocation = dest.latitude + "," + dest.longitude;
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destLocation);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        // Add FLAG_ACTIVITY_NEW_TASK flag because you are starting from a Service
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Check if there's an activity to handle this intent
        PackageManager packageManager = context.getPackageManager();
        if (mapIntent.resolveActivity(packageManager) != null) {
            context.startActivity(mapIntent);
        } else {
            // Handle error - Google Maps not available
            Toast.makeText(context, "Google Maps app not installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelRide() {
        DatabaseReference tripReference = FirebaseDatabase.getInstance().getReference("trips");
        tripReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Trip trip = snapshot.getValue(Trip.class);
                    if (trip != null && trip.getDriver_id().equals(getCurrentAccountId())) {
                        trip.setEnd_time(String.valueOf(System.currentTimeMillis()));
                        RequestManager requestManager = new RequestManager(getApplicationContext());
                        requestManager.clearRequest();
                        saveTripToFirestore(trip);
                        makeAvailable(trip.getDriver_id());
                    }
                }
                // You can pass this list to your UI or perform further operations

            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });

    }

    private void makeAvailable(String id) {
        taxiReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot dataSnapshot) {
                List<TaxiLocation> availableTaxis = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TaxiLocation taxiLocation = snapshot.getValue(TaxiLocation.class);
                    if (taxiLocation != null && taxiLocation.getDriverId().equals(getCurrentAccountId())) {
                        taxiLocation.setStatus("available");
                        taxiReference.child(getCurrentAccountId()).setValue(taxiLocation);
                        TaxisAvailable available = taxiLocation.createTaxiAvailble();
                        String category = taxiLocation.getTaxiInit().getCategory();
                        if (category.equals("Boda Boda")){
                            category = "BodaBoda";
                        }
                        availableRef.child(taxiLocation.getStatus())
                                .child(category)
                                .child(taxiLocation.getTaxiInit().getTaxi_id())
                                .setValue(available);
                        Intent tripIntent = new Intent(getApplicationContext(), TripActivity.class);
                        tripIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  // Required if starting from a service
                        tripIntent.putExtra("tripId", id);  // Pass any necessary data
                        startActivity(tripIntent);

                    }
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
    }

    private void saveTripToFirestore(Trip ride) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("trips")
                .document(ride.getId())
                .set(ride)
                .addOnSuccessListener(documentReference -> {
                    // Successfully added the ride to Firestore
                    // Handle success, if needed
                })
                .addOnFailureListener(e -> {
                    // Failed to add the ride to Firestore
                    // Handle failure, if needed
                });
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference tripsRef = database.getReference("trips");
        tripsRef.child(getCurrentAccountId()).setValue(ride);
    }

    private void startDriverSearchTimer() {
        binding.countdownTextView.setVisibility(View.VISIBLE);
        countDownTimer = new CountDownTimer(300000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutesRemaining = millisUntilFinished / 60000; // Convert milliseconds to minutes
                long secondsRemaining = (millisUntilFinished % 60000) / 1000; // Get the remaining seconds within the minute
                String timeRemaining = String.format("%02d:%02d",
                        minutesRemaining, secondsRemaining);
                binding.countdownTextView.setText(timeRemaining);
            }

            @Override
            public void onFinish() {
                binding.countdownTextView.setText("00:00");
                // You can add additional actions to perform when the timer finishes here.
            }
        }.start(); // Start the countdown timer
    }

}