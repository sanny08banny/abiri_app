package com.sanny_tech.carapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.FrequentLocAdapter;
import com.sanny_tech.carapp.adapters.SearchLocAdapter;
import com.sanny_tech.carapp.asynctasks.DirectionsLoader;
import com.sanny_tech.carapp.databinding.LayoutBottomSheetRideBinding;
import com.sanny_tech.carapp.dialogs.TaxisBottomSheet;
import com.sanny_tech.carapp.entities.Decline;
import com.sanny_tech.carapp.entities.Ride;
import com.sanny_tech.carapp.entities.TaxiLocation;
import com.sanny_tech.carapp.services.LocationUpdateService;
import com.sanny_tech.carapp.taxi_utils.DeclineManager;
import com.sanny_tech.carapp.taxi_utils.Trip;
import com.sanny_tech.carapp.taxi_utils.TripAnalyzer;
import com.sanny_tech.carapp.taxi_utils.TripItemListener;
import com.sanny_tech.carapp.utils.DriverLocationManager;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<List<LatLng>>, TaxisBottomSheet.TaxiBookingListener,
        DriverLocationManager.OnLocationChangedListener, RideManager.OnRideChangedListener,
        DeclineManager.OnDeclineListener, TripItemListener.TripItemListenerCallback {

    private static final int REQUEST_CODE = 7;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private static final int DIRECTIONS_LOADER_ID = 78;
    private static final double MAX_DISTANCE_METERS = 5000; // 5000 meters as an example
    private static final int ADD_NEW_NUMBER = 58;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tripItemListener = new TripItemListener(this,this);

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyAlGhvKajzrEZiLaY0XfF-yoPzQnxuKtGM");
        }
        placesClient = Places.createClient(this);
        firestore = FirebaseFirestore.getInstance();

        locationHelper = new LocationHelper(this);
        locationHelper.getCurrentLocation(new LocationHelper.LocationResultListener() {
            @Override
            public void onLocationResult(Location location) {
                currentLocation = location;
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        binding.pickupLocationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // This method is called to notify you that the characters are about to change
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // This method is called to notify you that the characters have changed
                String pickUpLoc = charSequence.toString();
                if (currentLocationName != null && !pickUpLoc.equals(currentLocationName)) {
                    isDest = false;
                    if (!pickUpLoc.isEmpty()) {
                        searchLocation(pickUpLoc, true);
                    } else {
                        binding.locationListView.setVisibility(View.GONE);
                    }
                }
                // Use the destination text as needed (e.g., for searching or displaying)
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // This method is called to notify you that the characters have changed and
                // after the text has been changed
            }
        });

        binding.destinationEditText.addTextChangedListener(new TextWatcher() {
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
                    binding.locationListView.setVisibility(View.GONE);
                }
                // Use the destination text as needed (e.g., for searching or displaying)
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // This method is called to notify you that the characters have changed and
                // after the text has been changed
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
// Inside your activity or fragment
        tapTargetView = TapTargetView.showFor(this,                 // Context
                TapTarget.forView(binding.destinationEditText,
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
                binding.destinationEditText.setFocusable(true);
            }
            binding.locationListView.setVisibility(View.GONE);
        });

        if (NewAppManager.getNewApp(this)) {
            showFirstTimePrompt();
        }
        driverLocationManager = new DriverLocationManager();
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

        binding.myTripsBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, MyTripsActivity.class);
                startActivity(intent);
            }
        });
        View floatingWindowLayout = binding.floatingWindow;
        ObjectAnimator collapseAnimator = ObjectAnimator.ofFloat(floatingWindowLayout, "translationY", 0, floatingWindowLayout.getHeight());
        collapseAnimator.setDuration(500);

        // Define the animation for expanding the window
        ObjectAnimator expandAnimator = ObjectAnimator.ofFloat(floatingWindowLayout, "translationY", floatingWindowLayout.getHeight(), 0);
        expandAnimator.setDuration(500);

        // Set up button click listener
        binding.collapseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (floatingWindowLayout.getVisibility() == View.VISIBLE) {
                    // Collapse the window
                    collapseAnimator.start();
                    binding.collapseButton.setImageResource(R.drawable.baseline_auto_awesome_motion_24);
                    floatingWindowLayout.setVisibility(View.GONE);
                } else {
                    // Expand the window
                    expandAnimator.start();
                    binding.collapseButton.setImageResource(R.drawable.baseline_close_24);
                    floatingWindowLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        getRidesByDriverId();
    }

    private void startLocationService() {
        Intent serviceIntent = new Intent(this, LocationUpdateService.class);
        startService(serviceIntent);
    }

    private void handleSelectedAddress(LatLng selectedAddress, String selectedAddressName) {
//        LatLng latLng = new LatLng(selectedAddress.getLatitude(), selectedAddress.getLongitude());
        if (destinationMarker != null) {
            destinationMarker.remove();
        }
        destinationMarker = mMap.addMarker(new MarkerOptions().position(selectedAddress).title(selectedAddressName));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedAddress, 15));


        // Calculate distance
        if (currentLocation != null) {
            float[] results = new float[1];
            Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                    selectedAddress.latitude, selectedAddress.longitude, results);

            double distance = results[0] * 0.001; // Distance in kilometers
            String distanceText = MessageFormat.format("Distance: {0} km", distance);

            showTaxisDialog(selectedAddressName, distanceText,
                    taxiLocations, distance, (float) selectedAddress.latitude, (float) selectedAddress.longitude);

            if (pickUpLatLan == null) {
                pickUpLatLan = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            }
            fetchDirections(pickUpLatLan, selectedAddress);
            Toast.makeText(this, MessageFormat.format("Distance: {0} km", distance), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show();
        }
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
        reference = FirebaseDatabase.getInstance().getReference("taxi_locations");
        ridesReference = FirebaseDatabase.getInstance().getReference("taxi_rides");
        declineReference = FirebaseDatabase.getInstance().getReference("declines");


        // Enable My Location button and related functionality
        mMap.setMyLocationEnabled(true);

        // Get last known location using Fused Location Provider API
        loadCurrentLocation();
        showSnackbar(binding.getRoot(), "Please wait as the taxis load");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentLocation != null) {
                    getAvailableTaxisNearby(currentLocation.getLatitude(), currentLocation.getLongitude());
                } else {
                    recreate();
                }
            }
        }, 6000);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                if (!isDialogShown) {
                    checkLocationForPointOfInterest(latLng);
                }
            }
        });
        loadRide();
    }

    private void loadCurrentLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLocation = location;
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
//                        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        setPickupLoc();
                    }
                });
    }

    private void getRidesByDriverId() {
        String userId = getCurrentAccountId();
        firestore.collection("trips")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
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
                            FrequentLocAdapter locAdapter = new FrequentLocAdapter(MapsActivity.this);
                            locAdapter.setAddressItems(frequentLocations);
                            binding.listView.setAdapter(locAdapter);
                            binding.listView.setOnItemClickListener((parent, view, position, id) -> {
                                searchLocation(locAdapter.getItem(position), false);
                            });


                        } else {

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                    }
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

    private void loadTaxis() {
        Log.e("Loading taxis", "Loading");
        if (taxiLocations.size() != 0) {
            Log.e("Loading taxis", String.valueOf(taxiLocations.size()));
            for (TaxiLocation taxiLocation : taxiLocations) {
                LatLng taxi = new LatLng(taxiLocation.getLatitude(), taxiLocation.getLongitude());
                BitmapDescriptor customMarker = BitmapDescriptorFactory.fromResource(R.drawable.transport);
                mMap.addMarker(new MarkerOptions()
                        .position(taxi)
                        .icon(customMarker)
                        .title("Taxi position"));
            }
        }
    }

    private void updateRideToFirebase(Ride ride) {
        ridesReference.child(ride.getDriver_id()).setValue(ride);
    }

    private void setPickupLoc() {
        if (currentLocation != null) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude(),
                        1);

                if (addresses != null && !addresses.isEmpty()) {
                    currentLocationName = addresses.get(0).getAddressLine(0);
                    binding.pickupLocationEditText.setText(currentLocationName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void getAvailableTaxisNearby(double userLatitude, double userLongitude) {
        // This method retrieves all available taxis nearby within a certain distance

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<TaxiLocation> availableTaxis = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TaxiLocation taxiLocation = snapshot.getValue(TaxiLocation.class);
                    if (taxiLocation != null && isWithinDistance(userLatitude, userLongitude,
                            Double.parseDouble(String.valueOf(taxiLocation.getLatitude())),
                            Double.parseDouble(String.valueOf(taxiLocation.getLongitude()))) &&
                            taxiLocation.getStatus().equals("available")) {
                        availableTaxis.add(taxiLocation);
                        taxiLocations.add(taxiLocation);
                    }
                }

                // Handle the available taxis list
                loadTaxis();
                // You can pass this list to your UI or perform further operations
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // Check if permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            }
        }
    }

    private void searchLocation(String locationName, boolean b) {
        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        List<String> locationNames = new ArrayList<>();
        AutocompleteSessionToken autocompleteSessionToken = AutocompleteSessionToken.newInstance();
        LatLngBounds kenyaBounds = new LatLngBounds(
                new LatLng(-4.6765, 33.9981), // South-west corner of Kenya
                new LatLng(4.6225, 41.9062)  // North-east corner of Kenya
        );
        RectangularBounds locationBias = RectangularBounds.newInstance(kenyaBounds);

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(locationName)
                .setLocationBias(locationBias)
                .setSessionToken(autocompleteSessionToken)
                .build();

        placesClient.findAutocompletePredictions(request).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<AutocompletePrediction> predictions = task.getResult().getAutocompletePredictions();
                int predictionsCount = predictions.size();
                AtomicInteger successCount = new AtomicInteger(0); // Counter to track successful fetchPlace operations

                for (AutocompletePrediction prediction : predictions) {
                    placesClient.fetchPlace(FetchPlaceRequest.newInstance(prediction.getPlaceId(), placeFields))
                            .addOnSuccessListener((fetchPlaceResponse) -> {
                                Place place = fetchPlaceResponse.getPlace();
                                if (isLocationInKenya(place.getLatLng())) {
                                    LatLng latLng = place.getLatLng();
                                    Address address = new Address(Locale.getDefault());
                                    address.setAddressLine(0, place.getAddress());
                                    addressList.put(address.getAddressLine(0), latLng);

                                    locationNames.add(address.getAddressLine(0));
                                }
                                successCount.incrementAndGet(); // Increment counter on success

                                // Check if all fetchPlace operations completed, update the adapter if needed
                                if (successCount.get() == predictionsCount) {
                                    updateAdapter(locationNames, b);
                                }
                            })
                            .addOnFailureListener((e) -> {
                                // Log the failure reason
                                Log.e("SearchLocation", "Fetch place request failed: " + e.getMessage());
                                // Increment counter on failure to ensure it's considered in completion check
                                successCount.incrementAndGet();
                                // Check if all fetchPlace operations completed, update the adapter if needed
                                if (successCount.get() == predictionsCount) {
                                    updateAdapter(locationNames, b);
                                }
                            });
                }
            } else {
                // Log the autocomplete request failure reason
                Log.e("SearchLocation", "Autocomplete request failed: " + task.getException().getMessage());
                Toast.makeText(this, "No locations found", Toast.LENGTH_SHORT).show();
            }
        });
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
            binding.locationListView.setVisibility(View.VISIBLE);
        } else {
            handleSelectedAddress(addressList.get(locationNames.get(0)), locationNames.get(0));
        }
    }

    private void showTaxisDialog(String selectesLoc, String distanceText, List<TaxiLocation> taxiLocations,
                                 double travelDistance, float destLat, float destLon) {
        storiOptionsBottomSheet = new TaxisBottomSheet(taxiLocations,
                selectesLoc, distanceText, currentLocation.getLatitude(), currentLocation.getLongitude(),
                travelDistance, destLat, destLon);
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
            return new DirectionsLoader(this, source, destination);
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
    public void onBookingResponse(boolean isSuccess, TaxiLocation item) {
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
                        tapTargetView.dismiss(true);
                        if (bottomSheetDialog == null) {
                            ride.setClientNumber(SimCardManager.getPhoneNumber(MapsActivity.this));
                            updateRideToFirebase(ride);
                            showRideBottomSheet(ride);
                        } else {
                            if (ride.getDriver_lat() != 0) {
                                LatLng driverPosition = new LatLng(ride.getDriver_lat(), ride.getDriver_lon());
                                updateDistanceText(calculateDistance(driverPosition, pickUpLatLan));
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
        rideBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.layout_bottom_sheet_ride, null, false);
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(rideBinding.getRoot());
        driverLocationManager.startLocationUpdates(MapsActivity.this);
        hideLoading();

        rideBinding.callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callDriver(ride.getDriverNumber());
            }
        });
        rideBinding.callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callDriver(ride.getDriverNumber());
            }
        });
        rideBinding.viewDriverLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.cancel();
                binding.showRide.setVisibility(View.VISIBLE);
                if (ride.getDriver_lat() != 0) {
                    LatLng latLng = new LatLng(ride.getDriver_lat(), ride.getDriver_lon());

                    addDriverMarker(latLng);
                }
            }
        });
        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.show();
    }

    private float calculateDistance(LatLng point1, LatLng point2) {
        Location location1 = new Location("point1");
        location1.setLatitude(point1.latitude);
        location1.setLongitude(point1.longitude);

        Location location2 = new Location("point2");
        location2.setLatitude(point2.latitude);
        location2.setLongitude(point2.longitude);

        return location1.distanceTo(location2);
    }

    // Update TextSwitcher with the calculated distance
    private void updateDistanceText(float distance) {
        String distanceString = distance + " meters"; // Format distance as needed

        // Update TextSwitcher with the new distance
        rideBinding.distanceFromPickUp.setText(distanceString);
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

    // Function to animate dots
    private void animateDots() {
        final TextView dot1 = findViewById(R.id.dot1);
        final TextView dot2 = findViewById(R.id.dot2);
        final TextView dot3 = findViewById(R.id.dot3);

        final Handler handler = new Handler(Looper.getMainLooper());

        final AnimationSet animationSet = new AnimationSet(true);
        final Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(500);
        fadeIn.setFillAfter(true);
        final Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(500);
        fadeOut.setFillAfter(true);

        animationSet.addAnimation(fadeIn);
        animationSet.addAnimation(fadeOut);
        animationSet.setRepeatCount(Animation.INFINITE);

        dot1.startAnimation(animationSet);

        handler.postDelayed(() -> dot2.startAnimation(animationSet), 200);
        handler.postDelayed(() -> dot3.startAnimation(animationSet), 400);
    }

    // Function to show loading
    private void showLoading() {
        // Show the dots animation
        binding.progressLt.getRoot().setVisibility(View.VISIBLE);
        animateDots();
    }

    // Function to hide loading
    private void hideLoading() {
        // Stop the dots animation
        binding.progressLt.getRoot().setVisibility(View.GONE);
        final TextView dot1 = findViewById(R.id.dot1);
        final TextView dot2 = findViewById(R.id.dot2);
        final TextView dot3 = findViewById(R.id.dot3);

        dot1.clearAnimation();
        dot2.clearAnimation();
        dot3.clearAnimation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NEW_NUMBER && resultCode == RESULT_OK && data != null) {
            String selectedNo = data.getStringExtra("selectedNo");

            showLoading();
            loadRide();
        }
    }

    private void showFirstTimePrompt() {
        // Create and configure an AlertDialog or DialogFragment
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Welcome!");
        builder.setMessage("You need to restart maps for the taxis to load.");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Mark that the user has seen the prompt
                NewAppManager.setNewApp(MapsActivity.this, false);
                recreate();
            }
        });

        builder.setCancelable(false);
        builder.show();
    }

    @Override
    public void onLocationChanged(Double latitude, Double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        addDriverMarker(latLng);
    }

    private void addDriverMarker(LatLng latLng) {
        BitmapDescriptor customMarker = BitmapDescriptorFactory.fromResource(R.drawable.driver);
        if (driverMarker != null) {
            driverMarker.setPosition(latLng);
        } else {
            driverMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(customMarker)
                    .title("Driver's Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    @Override
    public void onRideChanged(Ride ride) {
        boolean isLoading = binding.progressLt.getRoot().getVisibility() == View.VISIBLE;
        tripItemListener.startListening();
        if (isLoading) {
            loadRide();
            rideManager.stopRideUpdates();
        } else {
            rideManager.stopRideUpdates();
        }
    }

    @Override
    public void onDecline(Decline decline) {
        if (decline != null) {
            showRequestDeclinedAlert(decline);
            tripItemListener.stopListening();
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
                binding.destinationEditText.setText("");
                clearDecline(decline);
            }
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void clearDecline(Decline decline) {
        declineReference.child(decline.getDriver_id()).removeValue();
    }

    @Override
    public void onNewTripItem(Trip tripItem) {
        if (bottomSheetDialog != null){
            bottomSheetDialog.dismiss();
            showTripCompletePrompt(tripItem);
            tripItemListener.stopListening();
        }
    }
    private void showTripCompletePrompt(Trip trip) {
        // Create and configure an AlertDialog or DialogFragment
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Trip complete!");
        builder.setMessage("Driver has ended the trip. Charges for the journey: " +
                trip.getCharges());

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                recreate();
            }
        });

        builder.setCancelable(false);
        builder.show();
    }
}
