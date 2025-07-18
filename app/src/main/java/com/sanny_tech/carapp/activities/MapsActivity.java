package com.sanny_tech.carapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.FrequentLocAdapter;
import com.sanny_tech.carapp.adapters.SearchLocAdapter;
import com.sanny_tech.carapp.asynctasks.DirectionsLoader;
import com.sanny_tech.carapp.databinding.LayoutBottomSheetRideBinding;
import com.sanny_tech.carapp.dialogs.TaxisBottomSheet;
import com.sanny_tech.carapp.entities.AddressItem;
import com.sanny_tech.carapp.entities.Decline;
import com.sanny_tech.carapp.entities.Ride;
import com.sanny_tech.carapp.entities.TaxiLocation;
import com.sanny_tech.carapp.fragments.DriverMainFragment;
import com.sanny_tech.carapp.taxi_utils.DeclineManager;
import com.sanny_tech.carapp.taxi_utils.RouteCalculator;
import com.sanny_tech.carapp.taxi_utils.TaxiInit;
import com.sanny_tech.carapp.taxi_utils.TaxiViewModel;
import com.sanny_tech.carapp.taxi_utils.TravelDetails;
import com.sanny_tech.carapp.taxi_utils.Trip;
import com.sanny_tech.carapp.taxi_utils.TripAnalyzer;
import com.sanny_tech.carapp.taxi_utils.TripItemListener;
import com.sanny_tech.carapp.taxi_utils.Vehicle;
import com.sanny_tech.carapp.utils.DriverLocationManager;
import com.sanny_tech.carapp.utils.IpAddressManager;
import com.sanny_tech.carapp.utils.LocationHelper;
import com.sanny_tech.carapp.utils.NewAppManager;
import com.sanny_tech.carapp.utils.RideManager;
import com.sanny_tech.carapp.utils.SimCardManager;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sanny_tech.carapp.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<List<LatLng>>, TaxisBottomSheet.TaxiBookingListener,
        DriverLocationManager.OnLocationChangedListener, RideManager.OnRideChangedListener,
        DeclineManager.OnDeclineListener, TripItemListener.OnTripChangedListener,
        DriverLocationManager.OnTripStartListener, DriverLocationManager.OnTripCancelListener, OnMapsSdkInitializedCallback {

    private static final int REQUEST_CODE = 7;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private static final int DIRECTIONS_LOADER_ID = 78;
    private static final double MAX_DISTANCE_METERS = 5000; // 5000 meters as an example
    private static final int ADD_NEW_NUMBER = 58;
    private static final String CHANNEL_ID = "trip_channel";
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double currentLongitude, currentLatitude;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private DatabaseReference ridesReference, declineReference;
    private List<TaxiLocation> taxiLocations = new ArrayList<>();
    private SearchLocAdapter locationAdapter;
    private Map<String, LatLng> addressList = new HashMap<>();
    private Location currentLocation;
    private LatLng pickUpLatLan;
    private Handler handler = new Handler();
    private TaxisBottomSheet storiOptionsBottomSheet;
    private PlacesClient placesClient;
    private boolean isDest = false, isDialogShown = false;
    private String currentLocationName;
    private Drawable clearIcon;
    private Polyline polyline;
    private PolylineOptions polylineOptions;
    private Marker destinationMarker, driverMarker;
    private LayoutBottomSheetRideBinding rideBinding;
    private BottomSheetDialog bottomSheetDialog;
    private LocationHelper locationHelper;
    private DriverLocationManager driverLocationManager;
    private Ride ride;
    private RideManager rideManager;
    private DeclineManager declineManager;
    private TapTargetView tapTargetView;
    private FirebaseFirestore firestore;
    private TripItemListener tripItemListener;
    private String key;
    private HashMap<String, Marker> markers = new HashMap<>();
    private boolean isArrived = false;
    private String profileImage;
    private ListView listView;
    private TaxiLocation activeTaxi;
    private String baseUrl;
    private RouteCalculator routeCalculator;
    private CountDownTimer countDownTimer;
    private boolean isTrpStarted = false;
    private int SELECT_LOCATION_REQUEST_CODE = 20;
    private AddressItem addressItem;
    private Runnable updateCountdownRunnable;
    private AddressItem addressItem1;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private TaxiViewModel taxiViewModel;
    private LatLng driverLatLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tripItemListener = new TripItemListener(this);
        baseUrl = IpAddressManager.getIpAddress(this);
        routeCalculator = new RouteCalculator(this);
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        createNotificationChannel();

        key = getIntent().getStringExtra("key");
        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST, this);
        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(this,
                    key);
        }
        placesClient = Places.createClient(this);

        locationHelper = new LocationHelper(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        database = FirebaseDatabase.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("taxi_locations");
        ridesReference = FirebaseDatabase.getInstance().getReference("taxi_rides");
        declineReference = FirebaseDatabase.getInstance().getReference("declines");
        binding.pickupLocationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // This method is called to notify you that the characters are about to change
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // This method is called to notify you that the characters have changed
                Log.d("Edittext","changed");
                String pickUpLoc = charSequence.toString();
                Log.d("Edittext",pickUpLoc);
                if (currentLocationName != null && !pickUpLoc.equals(currentLocationName)) {
                    isDest = false;
                    Log.d("Edittext",pickUpLoc);
                    if (!pickUpLoc.isEmpty()) {
                        Log.d("Edittext",pickUpLoc);
                        searchLocation(pickUpLoc, true);
                    } else {
                        binding.searchBg.setVisibility(View.GONE);
                    }
                }else {
                    isDest = false;
                    searchLocation(pickUpLoc, true);
                }
                // Use the destination text as needed (e.g., for searching or displaying)
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // This method is called to notify you that the characters have changed and
                // after the text has been changed
            }
        });

        binding.customEdittext.destinationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // This method is called to notify you that the characters are about to change
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // This method is called to notify you that the characters have changed
                String destination = charSequence.toString();
                isDest = true;
                if (!destination.isEmpty()) {
                    searchLocation(destination, true);
                } else {
                    binding.searchBg.setVisibility(View.GONE);
                }
                // Use the destination text as needed (e.g., for searching or displaying)
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // This method is called to notify you that the characters have changed and
                // after the text has been changed
            }
        });
        binding.customEdittext.endIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSelectLocationActivity();
            }
        });
        clearIcon = ContextCompat.getDrawable(this, R.drawable.baseline_close_24);
        clearIcon.setTint(com.google.android.material.R.attr.imageButtonStyle);

        // Set the compound drawable with the clear icon
        binding.pickupLocationEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, clearIcon, null);

// Add an OnClickListener to the clear icon
        if (clearIcon != null) {
            clearIcon.setBounds(0, 0, clearIcon.getIntrinsicWidth(), clearIcon.getIntrinsicHeight());
            binding.pickupLocationEditText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (event.getRawX() >= binding.pickupLocationEditText.getRight() - binding.pickupLocationEditText.getCompoundDrawables()[2].getBounds().width()) {
                            // Clear the text when the clear icon is clicked
                            binding.pickupLocationEditText.setText("");
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        binding.progressLt.cancelRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference requestReference = database.getReference("verified_requests");
                requestReference.child(getCurrentAccountId()).removeValue();
                Toast.makeText(MapsActivity.this, "Trip canceled successfully", Toast.LENGTH_SHORT).show();
                recreate();
            }
        });


        locationAdapter = new SearchLocAdapter(this);
        binding.locationListView.setAdapter(locationAdapter);

        binding.locationListView.setOnItemClickListener((parent, view, position, id) -> {
//            Address selectedAddress = addressList.get(position);
            if (isDest) {
                handleSelectedAddress(addressList.get(locationAdapter.getItem(position)),
                        locationAdapter.getItem(position));
                Log.e(MapsActivity.class.getSimpleName(), addressList.get(locationAdapter.getItem(position)).toString());
            } else {
                pickUpLatLan = addressList.get(locationAdapter.getItem(position));
                currentLocationName = locationAdapter.getItem(position);
                binding.pickupLocationEditText.setText(currentLocationName);
//                binding.destinationEditText.setEnabled(true);
                binding.customEdittext.destinationEditText.setFocusable(true);
            }
            binding.searchBg.setVisibility(View.GONE);
        });

        if (NewAppManager.getNewApp(this)) {
            showFirstTimePrompt();
        }
        driverLocationManager = new DriverLocationManager();
        driverLocationManager.setOnTripStartListener(this);
        driverLocationManager.setOnTripCancelListener(this);
        rideManager = new RideManager(this);
        declineManager = new DeclineManager(this);

        binding.showRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ride != null) {
                    binding.showRide.setVisibility(View.GONE);
                    if (bottomSheetDialog != null) {
                        bottomSheetDialog.show();
                        loadRide();
                    } else {
                        loadRide();
                    }
                }
            }
        });
        getRidesByDriverId();
        binding.current.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "Fetching your location", Toast.LENGTH_SHORT).show();
                loadCurrentLocation();
            }
        });
        binding.destChip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove the chip or perform other actions
                binding.destChip.setVisibility(View.GONE);
                recreate();
            }
        });
        binding.destChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (storiOptionsBottomSheet != null) {
                    storiOptionsBottomSheet.show(getSupportFragmentManager(),
                            storiOptionsBottomSheet.getTag());
                }
            }
        });
        View headerView = binding.navView.getHeaderView(0);
        ImageView userImage = headerView.findViewById(R.id.user_image);
        TextView userName = headerView.findViewById(R.id.user_name);
        TextView userEmail = headerView.findViewById(R.id.user_email);
        listView = headerView.findViewById(R.id.listView);

        // Set user details (this could come from your user data)
        if (getCurrentAccountId() != null && getCurrentUserName() != null) {
            userName.setText(getCurrentUserName());
            userEmail.setText(getCurrentAccountEmail());
        }
        profileImage = getWallPaper();
        if (getCurrentAccountId() == null) {
        } else {
            if (getCurrentAccountId().isEmpty()) {
            } else {
                updateProfileImage(profileImage, userImage);
            }
        }

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, ManageProfiles.class);
                startActivity(intent);
            }
        });

        // Open drawer on button click
        binding.myTripsBt.setOnClickListener(v -> binding.drawerLayout.openDrawer(GravityCompat.START));

        // Handle navigation item clicks
        binding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_history) {
                startActivity(new Intent(MapsActivity.this, MyTripsActivity.class));
                return true;
            } else if (id == R.id.nav_show_guide) {
                showGuide();
                return true;
            }
            return false;
        });
        addressItem1 = getIntent().getParcelableExtra("item");
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public String getCurrentUserName() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserName", null);
    }

    public String getCurrentAccountEmail() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserEmail", null);
    }

    private String getWallPaper() {
        // Get a reference to SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        return sharedPreferences.getString("profilePic", null);
    }

    private void updateProfileImage(String selectedImage, ImageView imageView) {
        if (selectedImage != null) {
            Glide.with(this)
                    .load(selectedImage)
                    .override(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT) // Set thedesired width and height for resizing
                    .into(imageView);
        }
    }

    private void showGuide() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }
        tapTargetView = TapTargetView.showFor(this,                 // Context
                TapTarget.forView(binding.customEdittext.destinationEditText,
                                "Enter your destination", "Click here to input your destination")
                        .cancelable(true)
                        .transparentTarget(true)
                        .targetRadius(60),
                new TapTargetView.Listener() {
                    // Listener for actions after the tooltip is dismissed
                    @Override
                    public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                        // Add any further actions here if needed
                    }
                });
    }

    private void handleSelectedAddress(LatLng selectedAddress, String selectedAddressName) {
//        LatLng latLng = new LatLng(selectedAddress.getLatitude(), selectedAddress.getLongitude());
//        if (destinationMarker != null) {
//            destinationMarker.remove();
//        }
//        destinationMarker = mMap.addMarker(new MarkerOptions().position(selectedAddress).title(selectedAddressName));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedAddress, 15));
        if (pickUpLatLan == null) {
            if (currentLocation != null) {
                pickUpLatLan = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            }else {
                Toast.makeText(this, "Pick-up not set", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Calculate distance
        float[] results = new float[1];
        Location.distanceBetween(pickUpLatLan.latitude, pickUpLatLan.longitude,
                selectedAddress.latitude, selectedAddress.longitude, results);

        double distance = results[0] * 0.001; // Distance in kilometers
        String distanceText = MessageFormat.format("Distance: {0} km", distance);
        addressItem = new AddressItem(selectedAddressName, distance, selectedAddress.latitude,
                selectedAddress.longitude);
        showTaxisDialog(selectedAddressName, distanceText,
                taxiLocations, distance,
                (float) selectedAddress.latitude, (float) selectedAddress.longitude);
        binding.destChip.setText(selectedAddressName);
        binding.destChip.setVisibility(View.VISIBLE);
        binding.inputsLt.setVisibility(View.GONE);

        fetchDirections(pickUpLatLan, selectedAddress);
        Toast.makeText(this, MessageFormat.format("Distance: {0} km", distance), Toast.LENGTH_SHORT).show();
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
        LatLng sydney = new LatLng(-34, 151);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15));

        // Check and request location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }


        // Enable My Location button and related functionality
        mMap.setMyLocationEnabled(true);

        // Get last known location using Fused Location Provider API
        loadCurrentLocation();
        showSnackbar(binding.getRoot(), "Please wait as the taxis load");
        handler = new Handler();

        updateCountdownRunnable = new Runnable() {
            @Override
            public void run() {
                if (pickUpLatLan != null) {
                    getAvailableTaxisNearby(pickUpLatLan.latitude, pickUpLatLan.longitude);
                }else {
                    if (currentLocation != null) {
                        getAvailableTaxisNearby(currentLocation.getLatitude(), currentLocation.getLongitude());
                    } else {
                        handler.postDelayed(this, 10000); // Update every second
                        return;
                    }
                }
                handler.postDelayed(this, 15000); // Update every second
            }
        };

        // Start the countdown updates
        handler.post(updateCountdownRunnable);
        loadRide();
//        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(@NonNull LatLng latLng) {
//                if (!isDialogShown) {
//                    checkLocationForPointOfInterest(latLng);
//                }
//            }
//        });
    }

    private void loadCurrentLocation() {
        locationHelper.getCurrentLocation(new LocationHelper.LocationResultListener() {
            @Override
            public void onLocationResult(Location location) {
                currentLocation = location;
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
//                        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                setPickupLoc();
                if (addressItem1 != null){
                    handleSelectedAddress(new LatLng(addressItem1.getLatitude(),addressItem1.getLongitude()),
                            addressItem1.getAddress());
                }
            }
        });
    }
    private void checkLocationServices() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGpsEnabled && !isNetworkEnabled) {
            // Show a dialog to prompt the user to enable location services
            showLocationServicesDialog();
        } else {
            // Location services are enabled, proceed with getting the location
            loadCurrentLocation();
        }
    }

    private void showLocationServicesDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Enable Location Services")
                .setMessage("Location services are required for this app. Please enable them in settings.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MapsActivity.this, "Location services are required for this app." +
                                " Please enable them in settings.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .show();
    }
    private void getRidesByDriverId() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            firestore = FirebaseFirestore.getInstance();
            String userId = getCurrentAccountId();
            firestore.collection("trips")
                    .whereEqualTo("user_id", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Trip> receivedTrips = new ArrayList<>();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                Trip trip = documentSnapshot.toObject(Trip.class);
                                receivedTrips.add(trip);
                            }
                            List<String> frequentLocations = new ArrayList<>();
                            TripAnalyzer tripAnalyzer = new TripAnalyzer();
                            if (currentLocationName != null) {
                                frequentLocations.add(currentLocationName);
                            }
                            frequentLocations.addAll(
                                    tripAnalyzer.getTopThreeVisitedPlaces(receivedTrips));

                            // Update the UI on the main thread
                            handler.post(() -> {
                                FrequentLocAdapter locAdapter = new FrequentLocAdapter(MapsActivity.this);
                                locAdapter.setAddressItems(frequentLocations);
                                if (listView != null) {
                                    listView.setAdapter(locAdapter);
                                    listView.setOnItemClickListener((parent, view, position, id) -> {
                                        searchLocation(locAdapter.getItem(position), false);
                                    });
                                }
                            });
                        } else {
                            // Handle empty result set
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        handler.post(() -> {
                            // Update the UI to show error message
                        });
                    });
        });
    }

    private void checkLocationForPointOfInterest(LatLng latLng) {
        Toast.makeText(this, "Fetching location", Toast.LENGTH_SHORT).show();
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // Check the address type using Google Places API
                List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.TYPES);
                FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

                placesClient.findCurrentPlace(request).addOnSuccessListener((response) -> {
                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                        Place place = placeLikelihood.getPlace();
                        List<Place.Type> placeTypes = place.getTypes();

                        // Check if the place is a landmark or potential taxi destination
                        if (placeTypes.contains(Place.Type.POINT_OF_INTEREST) || placeTypes.contains(Place.Type.TAXI_STAND)) {
                            // This is a point of interest (landmark or potential taxi destination)
                            showLocationDetailsDialog(address.getAddressLine(0), latLng); // Function to display details in a dialog
                            return; // Exit the loop if a point of interest is found
                        }
                    }
                    // If no point of interest found among the likely places
                    // Handle accordingly, e.g., display a message or take other actions
                }).addOnFailureListener((exception) -> {
                    // Handle failure in finding current place
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showLocationDetailsDialog(String placeName, LatLng latLng) {
        isDialogShown = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);

        // Inflate the custom layout
        View dialogView = LayoutInflater.from(MapsActivity.this).inflate(R.layout.dialog_location_details, null);
        builder.setView(dialogView);

        TextView textViewLocationDetails = dialogView.findViewById(R.id.textViewLocationDetails);
        Button buttonGoHere = dialogView.findViewById(R.id.buttonGoHere);

        // Set location details
        String locationDetails = "Place Name: " + placeName;
        textViewLocationDetails.setText(locationDetails);
        AlertDialog dialog = builder.create();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isDialogShown = false;
            }
        });

        buttonGoHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSelectedAddress(latLng, placeName);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void updateRideToFirebase(Ride ride) {
        ridesReference.child(ride.getDriver_id()).setValue(ride);
    }

    private void setPickupLoc() {
        if (currentLocation != null) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(
                            currentLocation.getLatitude(),
                            currentLocation.getLongitude(),
                            1);

                    if (addresses != null && !addresses.isEmpty()) {
                        String addressLine = addresses.get(0).getAddressLine(0);
                        runOnUiThread(() -> {
                            if (binding != null) {
                                binding.pickupLocationEditText.setText(addressLine);
                            }
                        });
                    } else {
                        runOnUiThread(() -> showError("Unable to find address for the current location."));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> showError("Geocoding failed, please try again."));
                }
            });
        } else {
            showError("Current location is not available.");
        }
    }

    private void showError(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    public void getAvailableTaxisNearby(double userLatitude, double userLongitude) {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                executorService.submit(() -> {
                    // Process data on a background thread
                    processTaxiData(dataSnapshot, userLatitude, userLongitude);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
    }

    private void processTaxiData(DataSnapshot dataSnapshot, double userLatitude, double userLongitude) {
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            TaxiLocation taxiLocation = snapshot.getValue(TaxiLocation.class);
            if (taxiLocation != null && isWithinDistance(userLatitude, userLongitude,
                    taxiLocation.getLatitude(), taxiLocation.getLongitude()) &&
                    taxiLocation.getStatus().equals("available")) {
                runOnUiThread(() -> addOrUpdateMarker(taxiLocation));
            }
        }
    }

    private void addOrUpdateMarker(TaxiLocation taxiLocation) {
        LatLng position = new LatLng(taxiLocation.getLatitude(), taxiLocation.getLongitude());
        String driverId = taxiLocation.getDriverId();

        // Resize the marker icon
        int width = 40; // Adjust width as needed
        int height = 77; // Adjust height as needed
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.transport_car_taxi);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);
        BitmapDescriptor customMarker = BitmapDescriptorFactory.fromBitmap(resizedBitmap);

        if (markers.containsKey(driverId)) {
            // Update existing marker
            Marker marker = markers.get(driverId);
            if (marker != null) {
                float orientation = calculateBearing(marker.getPosition().latitude, marker.getPosition().longitude,
                        taxiLocation.getLatitude(), taxiLocation.getLongitude());
                marker.setPosition(position);
                marker.setRotation(orientation);
            }
        } else {
            // Add new marker
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .icon(customMarker)
                    .rotation(taxiLocation.getOrientation())
                    .anchor(0.5f, 0.5f)
                    .title("Driver ID: " + driverId)
                    .snippet("Seats: " + taxiLocation.getSeats() + ", Status: " + taxiLocation.getStatus()));
            markers.put(driverId, marker);
            taxiLocations.add(taxiLocation);
        }
    }


    private boolean isWithinDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = R * c; // Distance in kilometers

        // Convert distance to meters if required
        double distanceInMeters = distance * 1000;

        return distanceInMeters <= MAX_DISTANCE_METERS;
    }

    private float calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        double longitudeDiff = Math.toRadians(lon2 - lon1);
        double latitude1 = Math.toRadians(lat1);
        double latitude2 = Math.toRadians(lat2);

        double x = Math.sin(longitudeDiff) * Math.cos(latitude2);
        double y = Math.cos(latitude1) * Math.sin(latitude2) -
                Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longitudeDiff);

        double initialBearing = Math.atan2(x, y);
        // Convert from radians to degrees and normalize to 0-360
        initialBearing = Math.toDegrees(initialBearing);
        return (float) ((initialBearing + 360) % 360);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // Check if permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                Toast.makeText(this, "Using taxi hailing requires location permission!!!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

//    private void searchLocation(String locationName, boolean b) {
//        List<Place.Field> placeFields = Arrays.asList(
//                Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
//        List<String> locationNames = new ArrayList<>();
//        AutocompleteSessionToken autocompleteSessionToken = AutocompleteSessionToken.newInstance();
//        LatLngBounds kenyaBounds = new LatLngBounds(
//                new LatLng(-4.6765, 33.9981), // South-west corner of Kenya
//                new LatLng(4.6225, 41.9062)  // North-east corner of Kenya
//        );
//        RectangularBounds locationBias = RectangularBounds.newInstance(kenyaBounds);
//
//        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
//                .setQuery(locationName)
//                .setLocationBias(locationBias)
//                .setSessionToken(autocompleteSessionToken)
//                .build();
//
//        placesClient.findAutocompletePredictions(request).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                List<AutocompletePrediction> predictions = task.getResult().getAutocompletePredictions();
//                int predictionsCount = predictions.size();
//                AtomicInteger successCount = new AtomicInteger(0); // Counter to track successful fetchPlace operations
//
//                for (AutocompletePrediction prediction : predictions) {
//                    placesClient.fetchPlace(FetchPlaceRequest.newInstance(prediction.getPlaceId(), placeFields))
//                            .addOnSuccessListener((fetchPlaceResponse) -> {
//                                Place place = fetchPlaceResponse.getPlace();
//                                if (isLocationInKenya(place.getLatLng())) {
//                                    LatLng latLng = place.getLatLng();
//                                    Address address = new Address(Locale.getDefault());
//                                    address.setAddressLine(0, place.getAddress());
//                                    addressList.put(address.getAddressLine(0), latLng);
//
//                                    locationNames.add(address.getAddressLine(0));
//                                }
//                                successCount.incrementAndGet(); // Increment counter on success
//
//                                // Check if all fetchPlace operations completed, update the adapter if needed
//                                if (successCount.get() == predictionsCount) {
//                                    updateAdapter(locationNames, b);
//                                }
//                            })
//                            .addOnFailureListener((e) -> {
//                                // Log the failure reason
//                                Log.e("SearchLocation", "Fetch place request failed: " + e.getMessage());
//                                // Increment counter on failure to ensure it's considered in completion check
//                                successCount.incrementAndGet();
//                                // Check if all fetchPlace operations completed, update the adapter if needed
//                                if (successCount.get() == predictionsCount) {
//                                    updateAdapter(locationNames, b);
//                                }
//                            });
//                }
//            } else {
//                // Log the autocomplete request failure reason
//                Log.e("SearchLocation", "Autocomplete request failed: " + task.getException().getMessage());
//                Toast.makeText(this, "No locations found", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
private void searchLocation(String locationName, boolean isPickup) {
    List<String> locationNames = Collections.synchronizedList(new ArrayList<>());
    Map<String, LatLng> addressItems = new HashMap<>(); // Ensure this is accessible or use your existing one

    String url = String.format(Locale.US,
            "https://maps.googleapis.com/maps/api/geocode/json?address=%s&components=country:KE&key=%s",
            Uri.encode(locationName), key);

    RequestQueue queue = Volley.newRequestQueue(this);

    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                try {
                    JSONArray results = response.getJSONArray("results");
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject result = results.getJSONObject(i);
                        String formattedAddress = result.optString("formatted_address");

                        JSONObject location = result
                                .getJSONObject("geometry")
                                .getJSONObject("location");

                        double lat = location.getDouble("lat");
                        double lng = location.getDouble("lng");

                        LatLng latLng = new LatLng(lat, lng);
                        addressList.put(formattedAddress, latLng);
                        locationNames.add(formattedAddress);
                    }

                    updateAdapter(locationNames, isPickup);

                } catch (JSONException e) {
                    Log.e("GeocodeSearch", "Parsing error: " + e.getMessage());
                    Toast.makeText(this, "Location parsing failed", Toast.LENGTH_SHORT).show();
                }
            },
            error -> {
                Log.e("GeocodeSearch", "Request error: " + error.getMessage());
                Toast.makeText(this, "Failed to find location", Toast.LENGTH_SHORT).show();
            });

    queue.add(request);
}


    private boolean isLocationInKenya(LatLng latLng) {
        // Check if the latitude and longitude are within the bounds of Kenya
        return latLng.latitude >= -4.6765 && latLng.latitude <= 4.6225 &&
                latLng.longitude >= 33.9981 && latLng.longitude <= 41.9062;
    }

    private void updateAdapter(List<String> locationNames, boolean b) {
        if (b) {
            locationAdapter.clear();
            locationAdapter.setAddressItems(locationNames);
            binding.locationListView.setAdapter(locationAdapter);
            binding.searchBg.setVisibility(View.VISIBLE);
        } else {
            handleSelectedAddress(addressList.get(locationNames.get(0)), locationNames.get(0));
        }
    }

    private void showTaxisDialog(String selectesLoc, String distanceText,
                                 List<TaxiLocation> taxiLocations,
                                 double travelDistance, float destLat, float destLon) {
        if(storiOptionsBottomSheet != null) {
            storiOptionsBottomSheet = null;
        }
            storiOptionsBottomSheet = new TaxisBottomSheet(taxiLocations,
                    selectesLoc, distanceText, pickUpLatLan.latitude, pickUpLatLan.longitude,
                    travelDistance, destLat, destLon, this, addressItem.getAddress(),
                    binding.pickupLocationEditText.getText().toString());
            storiOptionsBottomSheet.setBookingListener(this);
            storiOptionsBottomSheet.show(getSupportFragmentManager(), storiOptionsBottomSheet.getTag());
    }

    private void fetchDirections(LatLng source, LatLng destination) {
        Bundle args = new Bundle();
        args.putParcelable("source", source);
        args.putParcelable("destination", destination);

        LoaderManager.getInstance(this).restartLoader(DIRECTIONS_LOADER_ID, args, this);
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
        if (polyline != null) {
            polyline.remove();
        }
        polylineOptions = new PolylineOptions();
        polylineOptions.addAll(routePoints);
        polylineOptions.width(10); // Set the width of the polyline
        polylineOptions.color(Color.BLUE); // Set color of the polyline
        polyline = mMap.addPolyline(polylineOptions);
        Log.e("routes", String.valueOf(routePoints.size()));
        BitmapDescriptor customMarker = BitmapDescriptorFactory.fromResource(R.drawable.current_location);
        mMap.addMarker(new MarkerOptions()
                .position(routePoints.get(0))
                .icon(customMarker)
                .title("Origin")
        );
        BitmapDescriptor customMarker1 = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin);
        mMap.addMarker(new MarkerOptions()
                .position(routePoints.get(routePoints.size() - 1))
                .icon(customMarker1)
                .title("Destination")
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                routePoints.get(routePoints.size() - 1), 13));


        polyline.setClickable(true);
        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(@NonNull Polyline polyline) {
                Toast.makeText(MapsActivity.this, "polyline selected", Toast.LENGTH_SHORT).show();
                storiOptionsBottomSheet.show(getSupportFragmentManager(), storiOptionsBottomSheet.getTag());
            }
        });

    }

    private void showSnackbar(View rootView, String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.blue));
        snackbar.show();
    }

    @Override
    public void onBookingResponse(boolean isSuccess, Vehicle item) {
        storiOptionsBottomSheet.dismiss();
        if (isSuccess) {
            // Handle successful booking response
            if (!SimCardManager.getPhoneNumber(this).equals("")) {
                showLoading();
                rideManager.startRideUpdates(this);
                declineManager.startRideUpdates(this);
                loadRide();
            } else {
                openPhoneNumberActivity();
            }
        } else {
            // Handle error in booking response
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void openPhoneNumberActivity() {
        Intent intent = new Intent(MapsActivity.this, AddPhoneNumberActivity.class);
        intent.putExtra("instruction", "verification");
        startActivityForResult(intent, ADD_NEW_NUMBER);
    }

    private void loadRide() {
        ridesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Ride> rides = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ride = snapshot.getValue(Ride.class);
                    if (ride != null && ride.getUser_id().equals(getCurrentAccountId())) {
                        if (bottomSheetDialog == null) {
                            ride.setClientNumber(SimCardManager.getPhoneNumber(MapsActivity.this));
                            updateRideToFirebase(ride);
                            showRideBottomSheet(ride);
                        } else {
                            if (ride.getDriver_lat() != 0) {
                                if (pickUpLatLan == null && currentLocation != null) {
                                    pickUpLatLan = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                                }
                                LatLng driverPosition = new LatLng(ride.getDriver_lat(), ride.getDriver_lon());
                                updateDistanceText(driverPosition, pickUpLatLan);
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

    private void showRideBottomSheet(Ride ride) {
        rideBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.layout_bottom_sheet_ride,
                null, false);
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(rideBinding.getRoot());
        driverLocationManager.startLocationUpdates(MapsActivity.this, getCurrentAccountId());
        hideLoading();
        rideBinding.callDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callDriver(ride.getDriverNumber());
            }
        });
        loadTaxiDetails(ride.getDriver_id());
        if (ride.getDriver_lat() != 0) {
            driverLatLng = new LatLng(ride.getDriver_lat(), ride.getDriver_lon());
            if (pickUpLatLan == null) {
                pickUpLatLan = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            }
            addDriverMarker(driverLatLng);
        }
        rideBinding.viewDriverLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.showRide.setVisibility(View.VISIBLE);
                if (pickUpLatLan != null && ride.getDestination() != null) {
                    fetchDirectionsCustom(pickUpLatLan, new LatLng(ride.getDestination().getLatitude(),
                            ride.getDestination().getLongitude()));
                    if (driverLatLng != null) {
                        addDriverMarker(driverLatLng);
                    }
                }
                bottomSheetDialog.dismiss();
            }
        });
        rideBinding.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.showRide.setVisibility(View.VISIBLE);
                bottomSheetDialog.cancel();
            }
        });
        bottomSheetDialog.setCancelable(false);
        if (isActivityInForeground(MapsActivity.class.getName())) {
            bottomSheetDialog.show();
        }
    }

    private void fetchDirectionsCustom(LatLng pickUpLatLan, LatLng latLng) {
        DirectionsLoader directionsLoader = new DirectionsLoader(this,pickUpLatLan,
                latLng,key);
        directionsLoader.forceLoad();
        directionsLoader.registerListener(79, new Loader.OnLoadCompleteListener<List<LatLng>>() {
            @Override
            public void onLoadComplete(@NonNull Loader<List<LatLng>> loader, @Nullable List<LatLng> data) {
                if (data != null){
                    if (polyline != null) {
                        polyline.remove();
                    }
                    polylineOptions = new PolylineOptions();
                    polylineOptions.addAll(data);
                    polylineOptions.width(10); // Set the width of the polyline
                    polylineOptions.color(Color.BLUE); // Set color of the polyline
                    polyline = mMap.addPolyline(polylineOptions);
                    Log.e("routes", String.valueOf(data.size()));
                    BitmapDescriptor customMarker = BitmapDescriptorFactory.fromResource(R.drawable.current_location);
                    mMap.addMarker(new MarkerOptions()
                            .position(data.get(0))
                            .icon(customMarker)
                            .title("Origin")
                    );
                    BitmapDescriptor customMarker1 = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin);
                    mMap.addMarker(new MarkerOptions()
                            .position(data.get(data.size() - 1))
                            .icon(customMarker1)
                            .title("Destination")
                    );
                }
            }
        });
    }

    private void loadTaxiDetails(String driverId) {
        DatabaseReference ridesReference = FirebaseDatabase.getInstance().getReference("taxi_locations");
        ridesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TaxiLocation taxiLocation = snapshot.getValue(TaxiLocation.class);
                    if (taxiLocation != null &&
                            taxiLocation.getDriverId().equals(driverId)) {
                        activeTaxi = taxiLocation;
                    }
                }
                if (activeTaxi != null && bottomSheetDialog != null) {
                    rideBinding.plate.setText(activeTaxi.getTaxiInit().getPlate_number());
                    rideBinding.desc.setText(MessageFormat.format("{0} {1}",
                            activeTaxi.getTaxiInit().getManufacturer(),
                            activeTaxi.getTaxiInit().getModel()));
                    glideImage(activeTaxi.getTaxiInit());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
    }

    private void glideImage(TaxiInit s) {
        if (s.getTaxi_images().size() != 0) {
            String endPoint = baseUrl + "/taxi/image/" + s.getDriver_id() + "/"
                    + s.getTaxi_images().get(0);
            Log.e(DriverMainFragment.class.getSimpleName(), endPoint);
            Glide.with(this)
                    .load(endPoint)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.local_taxi_fill) // Placeholder image while loading
                            .error(R.drawable.local_taxi_fill)      // Error image if loading fails
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(rideBinding.taxiImage);
        }
    }

    // Update TextSwitcher with the calculated distance
    private void updateDistanceText(LatLng driver, LatLng pickup) {
        routeCalculator.calculateTravelTimes(
                driver,
                pickup,
                ride.getDestination().toLatLng(), new RouteCalculator.TravelDetailsCallback() {
                    @Override
                    public void onTravelDetailsCalculated(TravelDetails driverToPickupDetails,
                                                          TravelDetails pickupToDestinationDetails) {
                        // Handle the calculated travel details
                        // Details include both time (in seconds) and distance (in meters)
                        rideBinding.distanceFromPickUp.setText("( " + driverToPickupDetails.getReadableDuration() + " ) " +
                                driverToPickupDetails.getReadableDistance() + " away");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        // Handle error
                    }
                });
    }

    private void callDriver(String driverNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + driverNumber));
        startActivity(intent);
    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }

    // Function to show loading
    private void showLoading() {
        // Show the dots animation
        startDriverSearchTimer();
        binding.progressLt.getRoot().setVisibility(View.VISIBLE);
    }

    // Function to hide loading
    private void hideLoading() {
        // Stop the dots animation
        binding.progressLt.getRoot().setVisibility(View.GONE);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NEW_NUMBER && resultCode == RESULT_OK && data != null) {
            String selectedNo = data.getStringExtra("selectedNo");
            showLoading();
            loadRide();
        } else if (requestCode == SELECT_LOCATION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            if (data.hasExtra("selectedAddress")) {
                addressItem = data.getParcelableExtra("selectedAddress");
                updateSelectedLocationText();
            }
        }
    }

    private void updateSelectedLocationText() {
        binding.customEdittext.destinationEditText.setText(addressItem.getAddress());
    }

    private void showFirstTimePrompt() {
//        // Create and configure an AlertDialog or DialogFragment
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Welcome!");
//        builder.setMessage("You need to restart maps for the taxis to load.");
//
//        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                // Mark that the user has seen the prompt
//                NewAppManager.setNewApp(MapsActivity.this, false);
//                recreate();
//            }
//        });
//
//        builder.setCancelable(false);
//        builder.show();
        NewAppManager.setNewApp(MapsActivity.this, false);
        recreate();
    }

    @Override
    public void onLocationChanged(Double latitude, Double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        addDriverMarker(latLng);
    }

    private void addDriverMarker(LatLng latLng) {
        Log.e("Driver marker", "lat :" + latLng.latitude + "lon :" + latLng.longitude);
        BitmapDescriptor customMarker = BitmapDescriptorFactory.
                fromResource(R.drawable.car);
        if (driverMarker != null) {
            driverMarker.setPosition(latLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        } else {
            mMap.clear();
            driverMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(customMarker)
                    .anchor(0.5f,0.5f)
                    .title("Driver's Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    @Override
    public void onRideChanged(Ride ride) {
        boolean isLoading = binding.progressLt.getRoot().getVisibility() == View.VISIBLE;
        tripItemListener.startTripUpdates(this);
        if (isActivityInForeground(MapsActivity.class.getName())) {
            if (isLoading) {
                loadRide();
                rideManager.stopRideUpdates();
                declineManager.stopRideUpdates();
            } else {
                rideManager.stopRideUpdates();
                declineManager.stopRideUpdates();
            }
        }
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

    @Override
    public void onDecline(Decline decline) {
        if (decline != null) {
            showRequestDeclinedAlert(decline);
            tripItemListener.stopTripUpdates();
        }
    }

    private void showRequestDeclinedAlert(Decline decline) {
        // Create and configure an AlertDialog or DialogFragment
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Decline message!");
        builder.setMessage("We're sorry driver has declined your request please request another taxi.");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                declineManager.stopRideUpdates();
                hideLoading();
                binding.customEdittext.destinationEditText.setText("");
                clearDecline(decline);
            }
        });

        builder.setCancelable(false);
        builder.show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (rideManager != null) {
            rideManager.startRideUpdates(this);
            declineManager.startRideUpdates(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the timer to avoid memory leaks
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void clearDecline(Decline decline) {
        declineReference.child(decline.getDriver_id()).removeValue();
    }
    private void showTripCompletePrompt(Trip trip) {
        if (isFinishing() || isDestroyed()) {
            return; // Don't show dialog if activity is not running
        }

        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Trip complete!");
            builder.setMessage("Driver has ended the trip. Charges for the journey: " + trip.getCharges());

            builder.setPositiveButton("OK", (dialog, which) -> {
                DatabaseReference tripsRef = FirebaseDatabase.getInstance().getReference("trips");
                tripsRef.child(trip.getDriver_id()).removeValue();
                tripItemListener.stopTripUpdates();
                finish();
            });

            builder.setCancelable(false);
            builder.show();
        });
    }

    @Override
    public void onTripChanged(Trip trip) {
        if (bottomSheetDialog != null) {
            bottomSheetDialog.dismiss();
            showTripCompletePrompt(trip);
            deleteTaxiLocationFromFirebase();
        }
    }

    private void startDriverSearchTimer() {
        countDownTimer = new CountDownTimer(60000, 1000) { // 60 seconds timer with 1 second interval
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                binding.progressLt.countdownTextView.setText(String.valueOf(secondsRemaining));
            }

            @Override
            public void onFinish() {
                hideLoading();
                database = FirebaseDatabase.getInstance();
                DatabaseReference requestReference = database.getReference("verified_requests");
                requestReference.child(getCurrentAccountId()).removeValue();
                Toast.makeText(MapsActivity.this, "We could not connect you to a driver. Try again.", Toast.LENGTH_SHORT).show();
                if (storiOptionsBottomSheet != null) {
                    storiOptionsBottomSheet.show(getSupportFragmentManager(),
                            storiOptionsBottomSheet.getTag());
                }
            }
        }.start(); // Start the countdown timer
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Ride Notifications";
            String description = "Notifications for ride updates";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onTripStart(Boolean isStarted, Double latitude, Double longitude) {
        if (isStarted != null) {
            if (isStarted) {
                showDriverArrivedNotification("Driver Arrived",
                        "Your driver has arrived at the pick-up location.");
                if (rideBinding != null && !isArrived) {
                    rideBinding.title.setText("Your Driver is Here, Confirm?");
                    rideBinding.taxiNotArrivedLt.setVisibility(View.GONE);
                    rideBinding.driverArrived.setVisibility(View.VISIBLE);
                    rideBinding.driverArrived.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            isArrived = true;
                            rideBinding.driverArrived.setVisibility(View.GONE);
                            rideBinding.taxiNotArrivedLt.setVisibility(View.VISIBLE);
                        }
                    });
                }
            } else {
                if (!isTrpStarted) {
                    showDriverArrivedNotification("Abiri",
                            "Your driver has started the trip.");
                    if (rideBinding != null) {
                        rideBinding.title.setText("Trip started");
                        rideBinding.distanceFromPickUp.setVisibility(View.GONE);
                    }
                    isTrpStarted = true;
                }else {
                    fetchDirections(new LatLng(latitude,longitude),
                            new LatLng(ride.getDestination().getLatitude(),
                                    ride.getDestination().getLongitude()));
                }
            }
        } else {
            routeCalculator.calculateSingleTravelDetails(pickUpLatLan,
                    new LatLng(latitude, longitude), new RouteCalculator.SingleTravelDetailsCallback() {
                        @Override
                        public void onSingleTravelDetailsCalculated(TravelDetails travelDetails) {
                            rideBinding.title.setText(MessageFormat.format(
                                    "You Arrive your destination in {0}",
                                    travelDetails.getReadableDuration()));
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }
                    });
        }
    }

    private void showDriverArrivedNotification(String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_abiri_3_foreground) // Replace with your app's icon
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }

    private void openSelectLocationActivity() {
        DatabaseReference hireListener = FirebaseDatabase.getInstance().getReference("configurations");
        hireListener.addValueEventListener(new ValueEventListener() {
            // Inside onDataChange method of ValueEventListener
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    String mapKey = "";
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String key = snapshot.getKey();
                        if ("maps_key".equals(key)) {
                            mapKey = snapshot.getValue(String.class);
                        }
                    }

                    if (mapKey != null) {
                        Intent intent = new Intent(MapsActivity.this, SelectLocationActivity.class);
                        // Set extra to indicate multiple selection mode if needed
                        intent.putExtra("isMultipleSelection", false);
                        intent.putExtra("key", mapKey);
                        intent.putExtra("activity", "taxi");
                        startActivityForResult(intent, SELECT_LOCATION_REQUEST_CODE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });
    }
    @Override
    public void onTripCancel(Boolean isStarted, String clientNumber) {
        Toast.makeText(this, "Trip canceled." + clientNumber,
                Toast.LENGTH_SHORT).show();
        deleteTaxiLocationFromFirebase();
        recreate();
    }
    private void deleteTaxiLocationFromFirebase() {

        // Get the driverId
        String driverId = getCurrentAccountId(); // Replace with your driver's ID

        DatabaseReference ridesReference = database.getReference("taxi_rides");
        ridesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Ride> rides = new ArrayList<>();
                if(dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Ride ride = snapshot.getValue(Ride.class);
                        if (ride != null && ride.getUser_id().equals(getCurrentAccountId())) {
                            ridesReference.child(getCurrentAccountId()).removeValue();
                        }
                    }
                    // You can pass this list to your UI or perform further operations
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
    }
    @Override
    public void onMapsSdkInitialized(MapsInitializer.Renderer renderer) {
        switch (renderer) {
            case LATEST:
                Log.d("MapsDemo", "The latest version of the renderer is used.");
                break;
            case LEGACY:
                Log.d("MapsDemo", "The legacy version of the renderer is used.");
                break;
        }
    }
}
