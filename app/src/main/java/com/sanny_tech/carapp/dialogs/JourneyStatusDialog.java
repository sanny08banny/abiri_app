package com.sanny_tech.carapp.dialogs;

import static android.content.Context.MODE_PRIVATE;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.entities.Ride;
import com.sanny_tech.carapp.entities.TaxiLocation;
import com.sanny_tech.carapp.taxi_utils.RouteCalculator;
import com.sanny_tech.carapp.taxi_utils.TaxisAvailable;
import com.sanny_tech.carapp.taxi_utils.TravelDetails;
import com.sanny_tech.carapp.services.FloatingOverlayService;
import com.sanny_tech.carapp.taxi_utils.Trip;
import com.sanny_tech.carapp.taxi_utils.TripActivity;
import com.sanny_tech.carapp.utils.JourneyStatusManager;
import com.sanny_tech.carapp.utils.LocationHelper;
import com.sanny_tech.carapp.utils.RequestManager;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class JourneyStatusDialog {

    private final ImageButton navigate;
    private final TextView duration;
    private final LocationHelper locationHelper;
    private TextView pickUpText;
    private AlertDialog dialog;
    private EditText chargesEditText;
    private Button startStopButton;
    private TextView destinationText;

    private JourneyStatusManager journeyStatusManager;
    private JourneyStatusListener journeyListener;
    private OnItemClickListener touch_listener;
    private Handler handler;
    private Runnable updateCountdownRunnable;
    private OnTripStartListener trip_listener;
    private Context context;
    private static final float ARRIVAL_RADIUS_METERS = 50.0f; // Define your arrival radius
    private boolean arrivedPickUp = false;

    public interface JourneyStatusListener {
        void onJourneyComplete(boolean isSuccess);
    }

    public void setBookingListener(JourneyStatusListener listener) {
        this.journeyListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(String item);
    }

    public void setOnTripStartListener(OnTripStartListener listener) {
        this.trip_listener = listener;
    }
    public interface OnTripStartListener {
        void onTripStart(boolean isStarted);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.touch_listener = listener;
    }


    public JourneyStatusDialog(Context context, String destination, String pick_up, LatLng dest,
                               LatLng latLngPickUp) {
        this.context = context;
        journeyStatusManager = new JourneyStatusManager(context);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_journey_status, null);
        builder.setView(dialogView);

        destinationText = dialogView.findViewById(R.id.destination);
        pickUpText = dialogView.findViewById(R.id.pick_up);
        duration = dialogView.findViewById(R.id.trip_duration);
        startStopButton = dialogView.findViewById(R.id.start_stop_button);
        navigate = dialogView.findViewById(R.id.navigate);
        locationHelper = new LocationHelper(context);
        handler = new Handler();

        // Initialize the countdown update runnable
        updateCountdownRunnable = new Runnable() {
            @Override
            public void run() {
                updateCountdown(latLngPickUp,dest);
                handler.postDelayed(this, 1000); // Update every second
            }
        };

        // Start the countdown updates
        handler.post(updateCountdownRunnable);

        pickUpText.setText(pick_up);
        destinationText.setText(MessageFormat.format("Journey destination is {0}", destination));
        destinationText.setEnabled(false); // Disable editing the destination initially

        if (journeyStatusManager.isJourneyStarted()) {
            duration.setText(MessageFormat.format(
                    "Trip started {0} ago", journeyStatusManager.getFormattedElapsedTime()));
        }else {

        }

        navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGoogleMaps(context, dest);
            }
        });
        // Make dialog non-modal

        dialog = builder.create();
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleStartStopButtonClick(context, dest, latLngPickUp);
            }
        });

        dialog.setCancelable(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                touch_listener.onItemClick(startStopButton.getText().toString());
            }
        });
    }


    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    private void handleStartStopButtonClick(Context context, LatLng dest, LatLng latLngPickUp) {
        if ("Start Trip".equals(startStopButton.getText().toString())) {
            trip_listener.onTripStart(true);
            if (!journeyStatusManager.isJourneyStarted()) {
                journeyStatusManager.setJourneyStarted(true);
            }else {
                duration.setText(MessageFormat.format(
                        "Journey started {0} ago",
                        journeyStatusManager.getFormattedElapsedTime()));
            }
            startTrip(true);
            RouteCalculator routeCalculator = new RouteCalculator(context);
            routeCalculator.calculateSingleTravelDetails(latLngPickUp, dest, new RouteCalculator.SingleTravelDetailsCallback() {
                @Override
                public void onSingleTravelDetailsCalculated(TravelDetails travelDetails) {
//                    startFloatingService(context, travelDetails.getDuration());
                    Toast.makeText(context,travelDetails.getReadableDuration() +
                            " drive", Toast.LENGTH_SHORT).show();
                    notifyClientTripStart();
                    openGoogleMaps(context, dest);
                    if (isServiceRunning(context,FloatingOverlayService.class)){
                        startFloatingService(context, latLngPickUp,dest);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    Toast.makeText(context, "Poor internet connection. Restart to load.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if ("Arrived".equals(startStopButton.getText().toString())) {
            notifyClient();
            arrivedPickUp = true;
            duration.setText("Waiting...");
            startStopButton.setText("Start Trip");
        } else if ("End Trip".equals(startStopButton.getText().toString()) ||
                "On the way...".equals(startStopButton.getText().toString()) ||
                "Started".equals(startStopButton.getText().toString())){
            trip_listener.onTripStart(false);
            journeyStatusManager.setJourneyStarted(false);
            stopServiceIfRunning(context, FloatingOverlayService.class);
            journeyListener.onJourneyComplete(true);
            startTrip(false);
            cancelRide();
        } else if ("Navigate to pick-up".equals(startStopButton.getText().toString())) {
            RouteCalculator routeCalculator = new RouteCalculator(context);
            routeCalculator.calculateSingleTravelDetails(latLngPickUp, latLngPickUp,
                    new RouteCalculator.SingleTravelDetailsCallback() {
                @Override
                public void onSingleTravelDetailsCalculated(TravelDetails travelDetails) {
//                    startFloatingService(context, travelDetails.getDuration());
                    Toast.makeText(context,travelDetails.getReadableDuration() +
                            " drive", Toast.LENGTH_SHORT).show();
                    notifyClientTripStart();
                    openGoogleMaps(context, latLngPickUp);
                    if (isServiceRunning(context,FloatingOverlayService.class)){
                        startFloatingService(context, latLngPickUp,dest);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    Toast.makeText(context, "Poor internet connection. Restart to load.", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void notifyClientTripStart() {
    }

    private void startFloatingService(Context context,
                                      LatLng latLngPickUp,
                                      LatLng latLngDest) {
        Intent intent = new Intent(context, FloatingOverlayService.class);
        intent.putExtra("pickup_lat", latLngPickUp.latitude);
        intent.putExtra("pickup_lng", latLngPickUp.longitude);
        intent.putExtra("dest_lat", latLngDest.latitude);
        intent.putExtra("dest_lng", latLngDest.longitude);
        ContextCompat.startForegroundService(context, intent);
    }

    private void openGoogleMaps(Context context, LatLng dest) {
        String destLocation = dest.latitude + "," +
                dest.longitude; // Replace with actual coordinates

        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destLocation);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        context.startActivity(mapIntent);

    }
    private void updateCountdown(LatLng latLngPickUp, LatLng dest) {
            locationHelper.getCurrentLocation(new LocationHelper.LocationResultListener() {
                @Override
                public void onLocationResult(Location location) {
                    updateOverlay(location,latLngPickUp,dest);
                }
            });
    }
    private void updateOverlay(Location location, LatLng pickupLatLng, LatLng destinationLatLng) {
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
            if (!arrivedPickUp) {
                if (!journeyStatusManager.isJourneyStarted()) {
                    startStopButton.setText("Arrived");
                }else {
                    startStopButton.setText("Start trip");
                }
            }else {
                if (journeyStatusManager.isJourneyStarted()) {
                    startStopButton.setText("Started");
                    String formattedElapsedTime = journeyStatusManager.getFormattedElapsedTime();
                    duration.setText(MessageFormat.format("Journey started {0}",
                            formattedElapsedTime));
                }
            }
        } else if (distanceToDestination <= ARRIVAL_RADIUS_METERS) {
            startStopButton.setText("End Trip");
        } else {
            if (journeyStatusManager.isJourneyStarted()) {
                startStopButton.setText("On the way...");
                String formattedElapsedTime = journeyStatusManager.getFormattedElapsedTime();
                duration.setText(MessageFormat.format("Journey started {0}",
                        formattedElapsedTime));
            }else {
                startStopButton.setText("Navigate to pick-up");
            }
        }
    }

    public boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void stopServiceIfRunning(Context context, Class<?> serviceClass) {
        if (isServiceRunning(context, serviceClass)) {
            Intent stopIntent = new Intent(context, serviceClass);
            context.stopService(stopIntent);
        }
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
    private void cancelRide() {
        DatabaseReference tripReference = FirebaseDatabase.getInstance().getReference("trips");
        tripReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Trip trip = snapshot.getValue(Trip.class);
                    if (trip != null && trip.getDriver_id().equals(getCurrentAccountId())) {
                        trip.setEnd_time(String.valueOf(System.currentTimeMillis()));
                        tripReference.child(trip.getDriver_id()).setValue(trip);
                        RequestManager requestManager = new RequestManager(context);
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
        DatabaseReference taxiReference = FirebaseDatabase.getInstance()
                .getReference("taxi_locations");
        taxiReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot dataSnapshot) {
                List<TaxiLocation> availableTaxis = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TaxiLocation taxiLocation = snapshot.getValue(TaxiLocation.class);
                    String userId = getCurrentAccountId();
                    Log.d("id used",taxiLocation.getDriverId());
                    if (taxiLocation != null &&
                            userId.equals(taxiLocation.getDriverId())) {
                        taxiLocation.setStatus("available");
                        taxiReference.child(getCurrentAccountId()).setValue(taxiLocation);
                        DatabaseReference availableRef = FirebaseDatabase.getInstance().getReference("taxis");
                        TaxisAvailable available = taxiLocation.createTaxiAvailble();
                        String category = taxiLocation.getTaxiInit().getCategory();
                        if (category.equals("Boda Boda")){
                            category = "BodaBoda";
                        }
                        availableRef.child(taxiLocation.getStatus())
                                .child(category)
                                .child(taxiLocation.getTaxiInit().getTaxi_id())
                                .setValue(available);

                        Intent tripIntent = new Intent(context, TripActivity.class);
                        tripIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // Required if starting from a service
                        tripIntent.putExtra("tripId", id);  // Pass any necessary data
                        context.startActivity(tripIntent);
                        dismiss();

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
    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
}
