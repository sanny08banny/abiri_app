package com.sanny_tech.carapp.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.AddressAdapter;
import com.sanny_tech.carapp.databinding.ActivitySelectLocationBinding;
import com.sanny_tech.carapp.entities.AddressItem;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.sanny_tech.carapp.utils.AnimationUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SelectLocationActivity extends AppCompatActivity implements
        SearchView.OnQueryTextListener, AdapterView.OnItemClickListener, OnMapReadyCallback {
    private ActivitySelectLocationBinding binding;
    private View selectedLocationsLt;
    private AddressAdapter nearbyAddressesAdapter;
    private double currentLongitude;
    private double currentLatitude;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private final static int REQUEST_CODE = 120;
    private SearchLocationTask searchTask;
    private boolean isMultipleSelectionEnabled = false;
    private List<AddressItem> selectedLocations = new ArrayList<>();
    private AddressAdapter selectedLocationsAdapter; // For selected locations
    private AddressItem selectedAddress;
    private GoogleMap mMap;
    private Marker currentMarker;
    private String key;
    private PlacesClient placesClient;
    private BitmapDescriptor customMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_location);

        String activity = getIntent().getStringExtra("activity");
        if (activity != null) {
            if (activity.equals("add_dest")) {
                binding.title3.setText("Where’s your space located?");
                binding.title4.setText("Your address will be visible to guests who wish to visit your space");
            } else if (activity.equals("taxi")) {
                binding.progressLt.setVisibility(View.GONE);
                binding.title3.setText("Where do you want to go");
                binding.title4.setText("Finding location ?\n" + "Enter destination name.\n" +
                        "Drag the marker or click on the map");
            }else if (activity.equals("car_hire")) {
                binding.title3.setText("Where’s your car available?");
                binding.title4.setText("Your address will be visible to guests who wish to visit your space");
            }
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        key = getIntent().getStringExtra("key");
        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(),
                    key);
        }
        placesClient = Places.createClient(this);
        nearbyAddressesAdapter = new AddressAdapter(this);
        binding.locationsList.setAdapter(nearbyAddressesAdapter);
        binding.locationsList.setOnItemClickListener(this);

        binding.searchView.setOnQueryTextListener(this);

        // Retrieve the selection type from the intent (default to single selection)
        boolean isMultipleSelection = getIntent().getBooleanExtra("isMultipleSelection", false);
        if (isMultipleSelection) {
            // Enable multiple selection mode if requested
            isMultipleSelectionEnabled = true;

        }

        getLocation();

        binding.searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.mainLt.setVisibility(View.GONE);
                binding.destLt.setVisibility(View.VISIBLE);
            }
        });
        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedAddress != null) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("selectedAddress", selectedAddress);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }
            }
        });
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();

                            // Set a default location
                            LatLng defaultLocation = new LatLng(currentLatitude, currentLongitude);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation,
                                    15));
                            customMarker = BitmapDescriptorFactory.
                                    fromResource(R.drawable.pin_1);
                            // Set a draggable marker
                            currentMarker = mMap.addMarker(new MarkerOptions()
                                    .position(defaultLocation).title("Selected Location")
                                    .icon(customMarker)
                                    .draggable(true));

                            Geocoder geocoder = new Geocoder(SelectLocationActivity.this, Locale.getDefault());
                            List<Address> addresses = null;
                            try {
                                addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (addresses != null && addresses.size() > 0) {
                                Address address = addresses.get(0);
                                String currentAddress = address.getAddressLine(0);
                                binding.currentLocationText.setText(String.format("Current Location: %s", currentAddress));
                            }

                            searchNearbyAddresses();
                        }
                    }
                });
    }

    private void searchNearbyAddresses() {
        List<Address> nearbyAddresses = getNearbyAddresses(currentLatitude, currentLongitude);
        displayNearbyAddresses(nearbyAddresses);
    }

    private List<Address> getNearbyAddresses(double latitude, double longitude) {
        List<Address> addresses = new ArrayList<>();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            addresses = geocoder.getFromLocation(latitude, longitude, 10);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addresses;
    }

    private void displayNearbyAddresses(List<Address> addresses) {
        List<AddressItem> addressItems = new ArrayList<>();
        for (Address address : addresses) {
            double distance = calculateDistance(address.getLatitude(), address.getLongitude());
            AddressItem item = new AddressItem(address.getAddressLine(0), distance,
                    address.getLatitude(), address.getLongitude());
            addressItems.add(item);
        }
        nearbyAddressesAdapter.setAddressItems(addressItems);
        nearbyAddressesAdapter.notifyDataSetChanged();
    }

    private double calculateDistance(double latitude, double longitude) {
        final double R = 6371; // Radius of the Earth in kilometers

        double latDistance = Math.toRadians(latitude - currentLatitude);
        double lonDistance = Math.toRadians(longitude - currentLongitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(currentLatitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        return distance;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (searchTask != null) {
            searchTask.cancelLoad();
        }
        searchTask = new SearchLocationTask(this, query);
        searchTask.registerListener(0, new Loader.OnLoadCompleteListener<List<Address>>() {
            @Override
            public void onLoadComplete(Loader<List<Address>> loader, List<Address> addresses) {
                if (addresses != null && !addresses.isEmpty()) {
                    displayNearbyAddresses(addresses);
                } else {
                }
            }
        });
        searchTask.startLoading();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (searchTask != null) {
            searchTask.cancelLoad();
        }
        searchTask = new SearchLocationTask(this, newText);
        searchTask.registerListener(0, new Loader.OnLoadCompleteListener<List<Address>>() {
            @Override
            public void onLoadComplete(Loader<List<Address>> loader, List<Address> addresses) {
                if (addresses != null && !addresses.isEmpty()) {
                    displayNearbyAddresses(addresses);
                } else {
                }
            }
        });
        searchTask.startLoading();
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedAddress = nearbyAddressesAdapter.getItem(position);
        if (selectedAddress != null) {
            String address = selectedAddress.getAddress();
            if (isMultipleSelectionEnabled) {
                // If multiple selections are enabled, add the address to the selected locations list
                selectedLocations.add(selectedAddress);
                selectedLocationsAdapter.addAddressItem(selectedAddress);
                selectedLocationsAdapter.notifyDataSetChanged();
                Log.e("Location selection", "selected locations: " + selectedLocations.size());
            } else {
                // If single selection is enabled, pass the address back to the calling activity
                binding.mainLt.setVisibility(View.VISIBLE);
                binding.destLt.setVisibility(View.GONE);
                binding.searchText.setText(selectedAddress.getAddress());
            }
        }
    }

    @Override
    public void onBackPressed() {
        proceed();
        super.onBackPressed();
    }

    private void proceed() {
        List<String> selectedAddresses = new ArrayList<>();
        for (AddressItem addressItem : selectedLocations) {
            String address = addressItem.getAddress();
            selectedAddresses.add(address);
        }
        if (isMultipleSelectionEnabled) {
            // If in multiple selection mode, pass back the list of selected locations
            Intent resultIntent = new Intent();
            resultIntent.putStringArrayListExtra("selectedLocations", new ArrayList<>(selectedAddresses));
            setResult(Activity.RESULT_OK, resultIntent);
        } else {
            deliverResults(selectedAddresses);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;


        // Enable dragging the marker
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                // Optional: Do something when the drag starts
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // Optional: Do something when the marker is being dragged
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                // Update the marker title or any other UI changes when the drag ends
                LatLng position = marker.getPosition();
                checkLocationForPointOfInterest(position);
                AnimationUtils.animateMarkerBounce(marker,mMap);
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                checkLocationForPointOfInterest(latLng);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                if (currentMarker != null){
                    currentMarker.setPosition(latLng);
                }
                AnimationUtils.animateMarkerBounce(currentMarker, mMap);
            }
        });
    }

    private static class SearchLocationTask extends AsyncTaskLoader<List<Address>> {

        private String query;

        public SearchLocationTask(Context context, String query) {
            super(context);
            this.query = query;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public List<Address> loadInBackground() {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocationName(query, 10);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        public void deliverResult(List<Address> data) {
            if (isStarted()) {
                super.deliverResult(data);
            }
        }
    }

    private void deliverResults(List<String> addresses) {
        Intent resultIntent = new Intent();
        if (isMultipleSelectionEnabled) {
            // In multiple selection mode, pass the list of selected locations
            resultIntent.putStringArrayListExtra("selectedLocations", new ArrayList<>(addresses));
        } else if (!addresses.isEmpty()) {
            // In single selection mode, pass the single selected location
            resultIntent.putExtra("selectedAddress", addresses.get(0));
        }
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void checkLocationForPointOfInterest(LatLng latLng) {
        Toast.makeText(this, "Fetching location", Toast.LENGTH_SHORT).show();
        Geocoder geocoder = new Geocoder(SelectLocationActivity.this, Locale.getDefault());
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
                            selectedAddress = new AddressItem(address.getAddressLine(0),
                                    calculateDistance(latLng.latitude, latLng.longitude),
                                    latLng.latitude, latLng.longitude);
                            binding.searchText.setText(address.getAddressLine(0));
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
}




