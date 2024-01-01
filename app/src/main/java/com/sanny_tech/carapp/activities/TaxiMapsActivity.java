package com.sanny_tech.carapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.asynctasks.DirectionsLoader;
import com.sanny_tech.carapp.asynctasks.DriverLoader;
import com.sanny_tech.carapp.dialogs.JourneyStatusDialog;
import com.sanny_tech.carapp.entities.Decline;
import com.sanny_tech.carapp.entities.Ride;
import com.sanny_tech.carapp.entities.TaxiLocation;
import com.sanny_tech.carapp.enums.TaxiActions;
import com.sanny_tech.carapp.services.LocationUpdateService;
import com.sanny_tech.carapp.taxi_utils.ClientRequest;
import com.sanny_tech.carapp.taxi_utils.LocationSearcher;
import com.sanny_tech.carapp.taxi_utils.Trip;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class TaxiMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<List<LatLng>>,JourneyStatusDialog.JourneyStatusListener,
LocationSearcher.LocationCallback{

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 9;
    private static final int DIRECTIONS_LOADER_ID = 6;
    private static final int ADD_NEW_NUMBER = 39;
    private static final double LATITUDE_TOLERANCE = 0.0001;
    private GoogleMap mMap;
    private ActivityTaxiMapsBinding binding;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private ClientRequest request;
    private LatLng currentLatLng, latLngDest;
    private DatabaseReference reference,taxiReference,declineReference;
    private FirebaseDatabase database;
    private TapTargetView tapTargetView;
    private PlacesClient placesClient;
    private FirebaseFirestore firestore;
    private Handler handler = new Handler();
    private LocationSearcher locationSearcher;
    private String destinationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTaxiMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyAlGhvKajzrEZiLaY0XfF-yoPzQnxuKtGM");
        }
        placesClient = Places.createClient(this);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("taxi_rides");
        taxiReference = database.getReference("taxi_locations");
        declineReference = database.getReference("declines");
        firestore = FirebaseFirestore.getInstance();
        locationSearcher = new LocationSearcher(this);

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

                            fetchDirections(currentLatLng, latLngDest);

                            handleRequestStatus(request);
                        }
                    }
                });

        binding.acceptLt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptRequest();
            }
        });
        binding.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptRequest();
            }
        });
        binding.declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                declineRequest();
            }
        });
        binding.declineLt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                declineRequest();
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
        request.setStatus("Accepted");
        DriverLoader loader = new DriverLoader(this, request, TaxiActions.ACCEPT);
        loader.forceLoad();
        loader.registerListener(7, new Loader.OnLoadCompleteListener<String>() {
            @Override
            public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                if (data != null) {
                    binding.confirmationStatus.setVisibility(View.GONE);
                    showSnackbar(binding.getRoot(), "Successful connection. Click start to navigate.");
                    if (request != null){
                        showRequestLayout();
                    }
                }
            }
        });

    }

    private void declineRequest() {
        request.setStatus("Cancelled");
        DriverLoader loader = new DriverLoader(this, request, TaxiActions.ACCEPT);
        loader.forceLoad();
        loader.registerListener(7, new Loader.OnLoadCompleteListener<String>() {
            @Override
            public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                if (data != null) {
                    binding.confirmationStatus.setVisibility(View.GONE);
                    Decline decline = new Decline(getCurrentAccountId(),request.getClient_id());
                    createDecline(decline);
                    showSnackbar(binding.getRoot(), "Successful request declined.");
                    finish();
                }
            }
        });
    }
    private void createDecline(Decline decline) {
        declineReference.child(getCurrentAccountId()).setValue(decline);
    }

    private void showSnackbar(View rootView, String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.blue));
        snackbar.setAction("Start", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SimCardManager.getPhoneNumber(TaxiMapsActivity.this).equals("")) {
                    Ride ride = new Ride(getCurrentAccountId(), request.getClient_id(),
                            String.valueOf(new Date()), SimCardManager.getPhoneNumber(TaxiMapsActivity.this), "");

                    createNewRideToFirebase(ride);
                }else {
                    openPhoneNumberActivity();
                }
            }
        });
        snackbar.show();
    }
    private void openPhoneNumberActivity() {
        Intent intent = new Intent(TaxiMapsActivity.this,AddPhoneNumberActivity.class);
        startActivityForResult(intent,ADD_NEW_NUMBER);
    }
    private void createNewRideToFirebase(Ride ride) {
        reference.child(getCurrentAccountId()).setValue(ride);
        makeUnavailable();
        showToolTip();
    }

    private void showToolTip() {
        // Code to show a tooltip when the user clicks on the pickup point
        tapTargetView = TapTargetView.showFor(
                this,
                TapTarget.forView(binding.acceptLt, "Navigate to Pickup Point", "Click here to start navigation to the pickup point using Google Maps.")
                        .transparentTarget(true)
                        .titleTextColor(R.color.white)
                        .cancelable(false )
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

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            tapTargetView.dismiss(true);
            startLocationService();
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
        if (request.getStatus() == null) {
            binding.confirmationStatus.setVisibility(View.VISIBLE);
            binding.userPhone.setText(MessageFormat.format("Clients number: {0}",
                    request.getUser_phone()));
            showAddress(latLngDest);
        } else if (request.getStatus().equals("Accepted")) {
            binding.confirmationStatus.setVisibility(View.GONE);
            loadRide();
        }
    }
    private void showAddress(LatLng latLngDest) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    latLngDest.latitude, latLngDest.longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                destinationName = address.getFeatureName();
                binding.selectedLoc.setText(destinationName);
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(routePoints);
        polylineOptions.width(10); // Set the width of the polyline
        polylineOptions.color(Color.BLUE); // Set color of the polyline
        mMap.addPolyline(polylineOptions);

        addMarkers();
    }

    private void addMarkers() {
        mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

        mMap.addMarker(new MarkerOptions().position(latLngDest).title("Destination Location"));
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NEW_NUMBER && resultCode == RESULT_OK && data != null){
            String selectedNo = data.getStringExtra("selectedNo");

            Ride ride = new Ride(getCurrentAccountId(), request.getClient_id(),
                    String.valueOf(new Date()), selectedNo, "");
            createNewRideToFirebase(ride);
        }
    }

    // Inside onResume() method
//    @Override
//    protected void onResume() {
//        super.onResume();
//        updateCurrentLocation();
//        loadRide();
//    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateCurrentLocation();
        loadRide();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationService();
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
                        if (areLatitudesEqual(ride.getDriver_lat() ,request.getCurrent_lat())){
                            showStartJourney();
                        }else {
                            Toast.makeText(TaxiMapsActivity.this,
                                    "You are yet to arrive pick-up point", Toast.LENGTH_SHORT).show();
                            showToolTip();
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
        Toast.makeText(this, "Arrived Pick-up", Toast.LENGTH_SHORT).show();
        if (latLngDest != null){
            JourneyStatusDialog journeyStatusDialog = new JourneyStatusDialog(
                    this,destinationName);
            journeyStatusDialog.setBookingListener(this);
            journeyStatusDialog.show();
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

    private void startLocationService() {
        Intent serviceIntent = new Intent(this, LocationUpdateService.class);
        startService(serviceIntent);
    }
    private void stopLocationService() {
        // Stop the location tracking service
        Intent intent = new Intent(this, LocationUpdateService.class);
        stopService(intent);
    }
    private void cancelRide(String chargesText) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Ride> rides = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ride ride = snapshot.getValue(Ride.class);
                    if (ride != null && ride.getDriver_id().equals(getCurrentAccountId())) {
                        reference.child(getCurrentAccountId()).removeValue();
                        saveRideToFirestore(new Trip(generateID(),ride.getDriver_id(),ride.getUser_id(),ride.getStart_time(),
                                chargesText, ride.getDriverNumber(),ride.getClientNumber(),binding.pickUp.getText().toString(),
                                binding.destination.getText().toString()));
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
        taxiReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<TaxiLocation> availableTaxis = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TaxiLocation taxiLocation = snapshot.getValue(TaxiLocation.class);
                    if (taxiLocation != null && taxiLocation.getDriverId().equals(getCurrentAccountId())) {
                        taxiLocation.setStatus("available");
                        taxiReference.child(getCurrentAccountId()).setValue(taxiLocation);
                    }
                }
                Intent intent = new Intent(TaxiMapsActivity.this, MapsActivity.class);
                startActivity(intent);
                // You can pass this list to your UI or perform further operations
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
    }

    @Override
    public void onJourneyComplete(boolean isSuccess, String chargesText) {
        if (isSuccess){
            cancelRide(chargesText);
        }
    }
    private void saveRideToFirestore(Trip ride) {
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
    }

    @Override
    public void onLocationFound(Place place, boolean isPickup) {
        if (isPickup){
            binding.pickUp.setText(place.getName());
        }else {
            binding.destination.setText(place.getName());
        }
    }
}