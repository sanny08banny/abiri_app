package com.sanny_tech.carapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.asynctasks.DirectionsLoader;
import com.sanny_tech.carapp.asynctasks.DriverLoader;
import com.sanny_tech.carapp.asynctasks.TaxiLoader;
import com.sanny_tech.carapp.dialogs.DriverCancelTripDialog;
import com.sanny_tech.carapp.dialogs.JourneyStatusDialog;
import com.sanny_tech.carapp.entities.Decline;
import com.sanny_tech.carapp.entities.LatLngCustom;
import com.sanny_tech.carapp.entities.Ride;
import com.sanny_tech.carapp.entities.TaxiLocation;
import com.sanny_tech.carapp.enums.ActionType;
import com.sanny_tech.carapp.enums.TaxiActions;
import com.sanny_tech.carapp.taxi_utils.ClientRequest;
import com.sanny_tech.carapp.taxi_utils.DriverAvailabilityManager;
import com.sanny_tech.carapp.taxi_utils.LocationSearcher;
import com.sanny_tech.carapp.taxi_utils.OrientationManager;
import com.sanny_tech.carapp.taxi_utils.RouteCalculator;
import com.sanny_tech.carapp.taxi_utils.TaxiRequest;
import com.sanny_tech.carapp.taxi_utils.TaxisAvailable;
import com.sanny_tech.carapp.taxi_utils.TravelDetails;
import com.sanny_tech.carapp.taxi_utils.Trip;
import com.sanny_tech.carapp.services.FloatingOverlayService;
import com.sanny_tech.carapp.taxi_utils.TripActivity;
import com.sanny_tech.carapp.utils.JourneyStatusManager;
import com.sanny_tech.carapp.utils.RequestManager;
import com.sanny_tech.carapp.utils.SimCardManager;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sanny_tech.carapp.databinding.ActivityTaxiMapsBinding;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class TaxiMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<List<LatLng>>, JourneyStatusDialog.JourneyStatusListener,
        LocationSearcher.LocationCallback, OrientationManager.OrientationListener,
        JourneyStatusDialog.OnTripStartListener, JourneyStatusDialog.OnItemClickListener, DriverCancelTripDialog.CancelTripListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 9;
    private static final int DIRECTIONS_LOADER_ID = 6;
    private static final int ADD_NEW_NUMBER = 39;
    private static final double LATITUDE_TOLERANCE = 0.0001;
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 29;
    private GoogleMap mMap;
    private ActivityTaxiMapsBinding binding;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private ClientRequest request;
    private LatLng currentLatLng;
    private LatLng latLngDest;
    private DatabaseReference reference,taxiReference,declineReference;
    private FirebaseDatabase database;
    private TapTargetView tapTargetView;
    private PlacesClient placesClient;
    private FirebaseFirestore firestore;
    private Handler handler = new Handler();
    private LocationSearcher locationSearcher;
    private String destinationName;
    private float orientation = 0.0f;
    private OrientationManager orientationManager;
    private JourneyStatusDialog journeyStatusDialog;
    private double charges;
    private String key;
    private Ride activeRide;
    private BroadcastReceiver tripRadarReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String rideId = intent.getStringExtra("ride_id");
            Log.e("Broadcaster","triggered");
            // Handle the received ride ID
            // Load the ride information using the rideId
        }
    };
    private RouteCalculator routeCalculator;
    private LatLng latLngPickUp;
    private String pick_up_name;
    private int REQUEST_CODE = 86;
    private CountDownTimer countDownTimer;
    private RequestManager requestManager;
    private Runnable updateCountdownRunnable;
    private Handler activeRideHandler;
    private boolean isCountdownRunning = false;
    private TaxiRequest pricingDetails;
    private DatabaseReference requestReference;
    private DriverAvailabilityManager availabilityManager;
    private JourneyStatusManager journeyStatusManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTaxiMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        LocalBroadcastManager.getInstance(this).registerReceiver(tripRadarReceiver,
                new IntentFilter("TripRadarNotification"));
        requestManager = new RequestManager(TaxiMapsActivity.this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        key = getIntent().getStringExtra("key");
        // Initialize Places API
        if (!Places.isInitialized() ) {
            Places.initialize(getApplicationContext(),
                    key);
        }
        routeCalculator = new RouteCalculator(this);

        placesClient = Places.createClient(this);
        orientationManager = new OrientationManager(this,this);
        orientationManager.startListening();
        availabilityManager = new DriverAvailabilityManager(TaxiMapsActivity.this);
        journeyStatusManager = new JourneyStatusManager(this);


        database = FirebaseDatabase.getInstance();
        reference = database.getReference("taxi_rides");
        taxiReference = database.getReference("taxi_locations");
        declineReference = database.getReference("declines");
        requestReference = database.getReference("verified_requests");
        firestore = FirebaseFirestore.getInstance();
        locationSearcher = new LocationSearcher(this,key);

        // Get last known location using Fused Location Provider API
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLocation = location;

                        if (getIntent() != null && getIntent().hasExtra("request")) {
                            request = getIntent().getParcelableExtra("request");

                            if (request.getStatus() != null && !request.getStatus().equals("Accepted")) {
                                acceptRequest();
                            }

                            currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            latLngDest = new LatLng(request.getDest_lat(), request.getDest_lon());
                            latLngPickUp = new LatLng(request.getCurrent_lat(),request.getCurrent_lon());


                            fetchDirections(currentLatLng, latLngDest);
                            destinationName = showAddress(latLngDest);
                            handleRequestStatus(request);
                            getIntent().getExtras().clear();
                        }else {
                            activeRide = getIntent().getParcelableExtra("ride");
                            if (activeRide != null){
                                if (requestManager.loadRequest() != null) {
                                    request = requestManager.loadRequest();
                                }
                                if (request != null) {
                                    currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                                    latLngDest = new LatLng(request.getDest_lat(), request.getDest_lon());
                                    latLngPickUp = new LatLng(request.getCurrent_lat(), request.getCurrent_lon());

                                    loadRide();

                                    destinationName = showAddress(latLngDest);

                                    fetchDirections(currentLatLng, latLngDest);
                                    if (request != null) {
                                        showRequestLayout();
                                    }
                                }else {
                                    RequestManager requestManager = new RequestManager(this);
                                    requestManager.clearRequest();
                                }
                            }else {
                                finish();
                            }

                        }
                    }
                });

        binding.acceptLt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TaxiMapsActivity.this, "Please wait...", Toast.LENGTH_SHORT).show();
                acceptRequest();
            }
        });
        binding.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptRequest();
                binding.acceptButton.setVisibility(View.GONE);
            }
        });
        binding.declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchPricingDetails();
            }
        });
        binding.declineLt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchPricingDetails();
            }
        });
        binding.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDriverCancelTripDialog();
            }
        });
        binding.startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStartJourney();
            }
        });

    }

    private void fetchPricingDetails() {
        Toast.makeText(this, "declining", Toast.LENGTH_SHORT).show();
        requestReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        TaxiRequest taxiRequest = dataSnapshot.getValue(TaxiRequest.class);
                        if (taxiRequest != null &&
                                taxiRequest.getPricing_details().getRider_id().equals(request.getSender_id())) {
                            pricingDetails = taxiRequest;
                        }
                    }
                }
                if (pricingDetails != null){
                    declineRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showRequestLayout() {
        binding.requestLt.setVisibility(View.VISIBLE);
        binding.clientUserName.setText(request.getUser_name());
        binding.callUser.setText(MessageFormat.format("Clients number: {0}",
                request.getUser_phone()));
        locationSearcher.checkLocationForPointOfInterest(
                true,request.getCurrent_lat(),request.getCurrent_lon(),
                this);
        if (destinationName != null){
            binding.destination.setText(destinationName);
        }
    }
    private void acceptRequest() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        request.setStatus("Accepted");
        DriverLoader loader = new DriverLoader(this, request, TaxiActions.ACCEPT,null);
        loader.forceLoad();
        loader.registerListener(7, new Loader.OnLoadCompleteListener<String>() {
            @Override
            public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                if (data != null) {
                    binding.confirmationStatus.setVisibility(View.GONE);
                    if (!SimCardManager.getPhoneNumber(TaxiMapsActivity.this).isEmpty()) {
                        LatLngCustom lngCustom = new LatLngCustom(latLngDest.latitude,latLngDest.longitude);
                        Ride ride = new Ride(getCurrentAccountId(), request.getSender_id(),
                                "",
                                SimCardManager.getPhoneNumber(TaxiMapsActivity.this), "",
                                lngCustom);
                        ride.setStatus("initialised");
                        requestManager.saveRequest(request);
                        createNewRideToFirebase(ride);
                    }else {
                        openPhoneNumberActivity();
                    }
                    showSnackbar(binding.getRoot(), "Successful connection. Click start to navigate.");
                    if (request != null){
                        showRequestLayout();
                    }
                }else {
                    Toast.makeText(TaxiMapsActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    binding.acceptButton.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void declineRequest() {
        request.setStatus("Cancelled");
        Toast.makeText(this, "Declining request", Toast.LENGTH_SHORT).show();
        TaxiLoader taxiLoader = new TaxiLoader(this,0.0,null, ActionType.DECLINE,
                "",pricingDetails,pricingDetails.getTaxi_category());
        taxiLoader.forceLoad();
        taxiLoader.registerListener(4, new Loader.OnLoadCompleteListener<String>() {
            @Override
            public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                boolean isSuccess = (data != null);
                if (data != null) {
                    Toast.makeText(TaxiMapsActivity.this,
                            "Ride declined successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(TaxiMapsActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void createDecline(Decline decline) {
        declineReference.child(getCurrentAccountId()).setValue(decline);
    }

    private void showSnackbar(View rootView, String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.blue));
        snackbar.show();
    }
    private void openPhoneNumberActivity() {
        Intent intent = new Intent(TaxiMapsActivity.this,AddPhoneNumberActivity.class);
        intent.putExtra("instruction","verification");
        startActivityForResult(intent,ADD_NEW_NUMBER);
    }
    private void createNewRideToFirebase(Ride ride) {
        requestReference.child(request.getSender_id()).removeValue();
        if (currentLocation != null) {
            ride.setDriver_lon((float) currentLocation.getLongitude());
            ride.setDriver_lat((float) currentLocation.getLatitude());
        }
        reference.child(request.getSender_id()).setValue(ride);
        makeUnavailable();
        createTripToFirebase();
        showToolTip();
        binding.startStopButton.setVisibility(View.VISIBLE);
        binding.startStopButton.setText("Navigate to pick-up");
    }

    private void showToolTip() {
        // Code to show a tooltip when the user clicks on the pickup point
        tapTargetView = TapTargetView.showFor(
                this,
                TapTarget.forView(binding.acceptLt, "Navigate to Pickup Point", "Click here to start navigation to the pickup point using Google Maps.")
                        .transparentTarget(true)
                        .titleTextColor(R.color.white)
                        .cancelable(true)
                        .tintTarget(true),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        // Code to redirect the user to Google Maps for navigation
                        openGoogleMaps();
                    }
                });

    }

    private void openGoogleMaps() {
        String pickupLocation = request.getCurrent_lat() + "," +
                request.getCurrent_lon(); // Replace with actual coordinates

        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + pickupLocation);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (!Settings.canDrawOverlays(this)) {
            // If permission is not granted, request it
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
        }else {
            Intent overlayIntent = new Intent(this, FloatingOverlayService.class);
            overlayIntent.putExtra("pickup_lat", latLngPickUp.latitude);
            overlayIntent.putExtra("pickup_lng", latLngPickUp.longitude);
            overlayIntent.putExtra("dest_lat", latLngDest.latitude);
            overlayIntent.putExtra("dest_lng", latLngDest.longitude);
            ContextCompat.startForegroundService(TaxiMapsActivity.this, overlayIntent);
        }
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            tapTargetView.dismiss(true);
            startActivity(mapIntent);
        }
    }

    private void deleteTaxiLocationFromFirebase() {
        // Get the driverId
        String driverId = getCurrentAccountId(); // Replace with your driver's ID

        // Delete the TaxiLocation object from Firebase Realtime Database
        reference.child(driverId).removeValue();
    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }

    private void handleRequestStatus(ClientRequest request) {
        pick_up_name = showAddress(latLngPickUp);
        if (request.getStatus() == null) {
            binding.confirmationStatus.setVisibility(View.VISIBLE);
            routeCalculator.calculateTravelTimes(currentLatLng, latLngPickUp, latLngDest, new RouteCalculator.TravelDetailsCallback() {
                @Override
                public void onTravelDetailsCalculated(TravelDetails driverToPickupDetails,
                                                      TravelDetails pickupToDestinationDetails) {
                    // Handle the calculated travel details
                    // Details include both time (in seconds) and distance (in meters)
                    binding.selectedStartLoc.setText("( " + driverToPickupDetails.getReadableDuration() + " ) " +
                            driverToPickupDetails.getReadableDistance() + " away\n" + pick_up_name);
                    binding.selectedLoc.setText("( " + pickupToDestinationDetails.getReadableDuration() + " ) " +
                            pickupToDestinationDetails.getReadableDistance() + " trip\n" + destinationName);
                }

                @Override
                public void onError(Throwable throwable) {
                    // Handle error
                }
            });
            handleCharges();
            startDriverSearchTimer();
        } else if (request.getStatus().equals("Accepted")) {
            binding.confirmationStatus.setVisibility(View.GONE);
            loadRide();
        }
    }

    private void handleCharges() {
        float[] results = new float[1];
        Location.distanceBetween(request.getCurrent_lat(), request.getCurrent_lon(),
                request.getDest_lat(), request.getDest_lon(), results);

        double distance = results[0] * 0.001;
        DriverAvailabilityManager availabilityManager = new DriverAvailabilityManager(this);

        charges = request.getPrice();
        Locale kenyanLocale = new Locale("sw", "KE");
        Currency kenyanShilling = Currency.getInstance("KES");
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(kenyanLocale);
        numberFormat.setCurrency(kenyanShilling);
        String formattedAmount = numberFormat.format(charges);

        binding.price.setText(formattedAmount);
    }

    private String showAddress(LatLng latLngDest) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    latLngDest.latitude, latLngDest.longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                return address.getFeatureName();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
// Check and request location permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Enable My Location button and related functionality
        mMap.setMyLocationEnabled(true);

        if (currentLocation != null) {
            LatLng currentLocationLat = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLat, 15));
        }
    }

    private void fetchDirections(LatLng source, LatLng destination) {
        Bundle args = new Bundle();
        args.putParcelable("source", source);
        args.putParcelable("destination", destination);

        LoaderManager.getInstance(this).initLoader(DIRECTIONS_LOADER_ID, args, this);
    }

    @NonNull
    @Override
    public Loader<List<LatLng>> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == DIRECTIONS_LOADER_ID) {
            LatLng source = args.getParcelable("source");
            LatLng destination = args.getParcelable("destination");
            return new DirectionsLoader(this, source, destination, key);
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<LatLng>> loader, List<LatLng> data) {
        if (loader.getId() == DIRECTIONS_LOADER_ID) {
            if (data != null && mMap != null) {
                drawRouteOnMap(data);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<LatLng>> loader) {
        // Clear any resources if needed
    }

    private void drawRouteOnMap(List<LatLng> routePoints) {
        mMap.clear();
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(routePoints);
        polylineOptions.width(10); // Set the width of the polyline
        polylineOptions.color(Color.BLUE); // Set color of the polyline
        mMap.addPolyline(polylineOptions);

        addMarkers();
    }

    private void addMarkers() {
        BitmapDescriptor customMarker = BitmapDescriptorFactory.fromResource(R.drawable.current_location);
        BitmapDescriptor customMarker1 = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin);

        mMap.addMarker(new MarkerOptions().position(currentLatLng).icon(customMarker).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14));

        mMap.addMarker(new MarkerOptions().position(latLngDest).icon(customMarker1).title("Destination Location"));
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NEW_NUMBER && resultCode == RESULT_OK && data != null){
            String selectedNo = data.getStringExtra("selectedNo");
            LatLngCustom lngCustom = new LatLngCustom(latLngDest.latitude,latLngDest.longitude);
            Ride ride = new Ride(getCurrentAccountId(), request.getSender_id(), "",
                    selectedNo, "",lngCustom);
            ride.setStatus("initialised");
            createNewRideToFirebase(ride);
        }
    }

    // Inside onResume() method
//    @Override
//    protected void onResume() {
//        super.onResume();
//        updateCurrentLocation();
//        loadRide();
//        Log.e("On resume","resumed");
//    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateCurrentLocation();
        loadRide();
        Log.e("On restart","restart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        orientationManager.stopListening();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(tripRadarReceiver);
    }
    private void loadRide() {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Ride> rides = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ride ride = snapshot.getValue(Ride.class);
                    if (ride != null && ride.getDriver_id().equals(getCurrentAccountId())) {

                        ride.setDriver_lat((float) currentLocation.getLatitude());
                        ride.setDriver_lon((float) currentLocation.getLongitude());

                        Location driverLocation = new Location("");
                        driverLocation.setLatitude(currentLocation.getLatitude());
                        driverLocation.setLongitude(currentLocation.getLongitude());

                        Location pickupLocation = new Location("");
                        pickupLocation.setLatitude(request.getCurrent_lat());
                        pickupLocation.setLongitude(request.getCurrent_lon());

                        float distanceInMeters = driverLocation.distanceTo(pickupLocation);
                        Log.e("Distance to pickup", String.valueOf(distanceInMeters));

                        if (distanceInMeters <= 100) { // ✅ within 50 meters of pickup
                            if (!Settings.canDrawOverlays(TaxiMapsActivity.this)) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:" + getPackageName()));
                                startActivityForResult(intent, REQUEST_CODE);
                            } else {
                                showStartJourney();
                            }
                    }else {
                            binding.startStopButton.setVisibility(View.VISIBLE);
                            binding.startStopButton.setText("You are yet to arrive pick-up point");
                            JourneyStatusManager statusManager = new JourneyStatusManager(TaxiMapsActivity.this);
                            if (!statusManager.isJourneyStarted()) {
                                Toast.makeText(TaxiMapsActivity.this,
                                        "You are yet to arrive pick-up point", Toast.LENGTH_SHORT).show();
                                showToolTip();
                            }else {
                                Toast.makeText(TaxiMapsActivity.this,
                                        "Navigating to destination.", Toast.LENGTH_SHORT).show();
                            }
                        }
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

    private void showStartJourney() {
        binding.startStopButton.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Arrived Pick-up", Toast.LENGTH_SHORT).show();
        if (latLngDest != null){
            if (journeyStatusDialog == null) {
                journeyStatusDialog = new JourneyStatusDialog(
                        this, destinationName,binding.pickUp.getText().toString(),latLngDest,
                        latLngPickUp);
                journeyStatusDialog.setOnTripStartListener(this);
                journeyStatusDialog.setBookingListener(this);
                journeyStatusDialog.setOnItemClickListener(this);
                journeyStatusDialog.show();
            }else {
                journeyStatusDialog.show();
            }
        }
    }

    public boolean areLatitudesEqual(double latitude1, double latitude2) {
        return Math.abs(latitude1 - latitude2) <= 0.001;
    }
    private void updateCurrentLocation() {
        // Use FusedLocationProviderClient (Google Play Services) to get the current location
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // Use the retrieved location (location.getLatitude(), location.getLongitude())
                            // Update your current location information here
                            // For example, update UI elements with the new location
                            currentLocation = location;
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure to retrieve location
                    }
                });
    }

    private String generateID() {
        return UUID.randomUUID().toString();
    }

    private void makeUnavailable() {
        taxiReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<TaxiLocation> availableTaxis = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TaxiLocation taxiLocation = snapshot.getValue(TaxiLocation.class);
                    if (taxiLocation != null && taxiLocation.getDriverId().equals(getCurrentAccountId())) {
                        taxiLocation.setStatus("unavailable");
                        taxiReference.child(getCurrentAccountId()).setValue(taxiLocation);
                        DatabaseReference availableRef = FirebaseDatabase.getInstance().getReference("taxis");
                        availableRef.child("available")
                                .child(taxiLocation.getTaxiInit().getCategory())
                                .child(taxiLocation.getTaxiInit().getTaxi_id()).removeValue();
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
    private void makeAvailable() {
        OrientationManager orientationManager = new OrientationManager(this,this);
        taxiReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<TaxiLocation> availableTaxis = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TaxiLocation taxiLocation = snapshot.getValue(TaxiLocation.class);
                    if (taxiLocation != null &&
                            taxiLocation.getDriverId().equals(getCurrentAccountId())) {
                        taxiLocation.setStatus("available");
                        taxiLocation.setOrientation(orientation);
                        taxiReference.child(getCurrentAccountId()).setValue(taxiLocation);
                        TaxisAvailable available = taxiLocation.createTaxiAvailble();
                        String category = taxiLocation.getTaxiInit().getCategory();
                        if (category.equals("Boda Boda")){
                            category = "BodaBoda";
                        }
                        DatabaseReference availableRef = FirebaseDatabase.getInstance().getReference("taxis");
                        availableRef.child(taxiLocation.getStatus())
                                .child(category)
                                .child(taxiLocation.getTaxiInit().getTaxi_id())
                                .setValue(available);
                            finish();

                    }
                }
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
    }

    @Override
    public void onJourneyComplete(boolean isSuccess) {
//        if (isSuccess){
//            stopServiceIfRunning(TaxiMapsActivity.this, FloatingOverlayService.class);
//            cancelRide();
//        }
    }
    private void saveTripToFirestore(Trip ride) {
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
        DatabaseReference tripsRef = database.getReference("trips");
        tripsRef.child(getCurrentAccountId()).setValue(ride);
    }

    @Override
    public void onLocationFound(Place place, boolean isPickup) {
        if (place != null) {
            if (isPickup) {
                binding.pickUp.setText(place.getName());
            } else {
                binding.destination.setText(place.getName());
            }
        }
    }

    @Override
    public Void onOrientationChanged(float azimuth) {
        orientation = azimuth;
        return null;
    }
    private void startDriverSearchTimer() {
        countDownTimer = new CountDownTimer(30000, 1000) { // 60 seconds timer with 1 second interval
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                binding.countdownTextView.setText(String.valueOf(secondsRemaining));
            }

            @Override
            public void onFinish() {
               if (request != null && request.getStatus() != null && !request.getStatus().equals("Accepted")){
                   finish();
               } else if (request != null && request.getStatus() == null) {
                   finish();
               }
            }
        }.start(); // Start the countdown timer
    }
    private void createTripToFirebase() {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Ride> rides = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ride ride = snapshot.getValue(Ride.class);
                    if (ride != null && ride.getDriver_id().equals(getCurrentAccountId())) {
                        saveTripToFirestore(new Trip(generateID(),ride.getDriver_id(),
                                ride.getUser_id(),String.valueOf(System.currentTimeMillis()),"",
                                String.valueOf(charges), ride.getDriverNumber(),
                                ride.getClientNumber(),pick_up_name,
                                destinationName));
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
    private void startTrip(boolean isStarted, String reason) {
        DatabaseReference tripStartReference = FirebaseDatabase.getInstance().getReference("taxi_rides");
        tripStartReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Ride> rides = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ride ride = snapshot.getValue(Ride.class);
                    if (ride != null && ride.getDriver_id().equals(getCurrentAccountId())) {
                        if (isStarted) {
                            ride.setStatus("started");
                            ride.setStart_time(String.valueOf(System.currentTimeMillis()));
                        }else {
                            ride.setStatus("canceled");
                            ride.setClientNumber(reason);

                        }
                        availabilityManager.saveAvailabilityStatus(false);
                        journeyStatusManager.setJourneyStarted(false);
                        tripStartReference.child(getCurrentAccountId()).setValue(ride);
                        requestManager.clearRequest();
                        makeAvailable();
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
    @Override
    public void onTripStart(boolean isStarted) {

    }


    @Override
    public void onItemClick(String item) {
        activeRideHandler = new Handler();

        // Initialize the countdown update runnable
        updateCountdownRunnable = new Runnable() {
            @Override
            public void run() {
                updateCountdown();
                handler.postDelayed(this, 1000); // Update every second
            }
        };

        startCountdown();

    }
    private void updateCountdown() {
        JourneyStatusManager journeyStatusManager = new JourneyStatusManager(this);
        if (journeyStatusManager.isJourneyStarted()) {
            String formattedElapsedTime = journeyStatusManager.getFormattedElapsedTime();
            binding.startStopButton.setText(formattedElapsedTime);
        } else {
            binding.startStopButton.setText("Journey not started");
        }
    }
    private void startCountdown() {
        if (!isCountdownRunning) {
            handler.post(updateCountdownRunnable);
            isCountdownRunning = true;
        }
    }

    // Method to stop the countdown updates
    private void stopCountdown() {
        if (isCountdownRunning) {
            handler.removeCallbacks(updateCountdownRunnable);
            isCountdownRunning = false;
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
    private void showDriverCancelTripDialog() {
        DriverCancelTripDialog dialog = new DriverCancelTripDialog(this, this);
        dialog.show();
    }

    @Override
    public void onCancelTrip(String reason) {
        Toast.makeText(this, "Trip cancelled: " + reason,
                Toast.LENGTH_SHORT).show();
        stopServiceIfRunning(this, FloatingOverlayService.class);
        startTrip(false,reason);
        finish();
    }
}