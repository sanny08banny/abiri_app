package com.sanny_tech.carapp.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.DriverDocsActivity;
import com.sanny_tech.carapp.activities.DriverHistoryActivity;
import com.sanny_tech.carapp.activities.TaxiMapsActivity;
import com.sanny_tech.carapp.adapters.CustomSpinnerAdapter;
import com.sanny_tech.carapp.adapters.PaymentAdapter;
import com.sanny_tech.carapp.adapters.TripAdapter;
import com.sanny_tech.carapp.asynctasks.FunctionsLoader;
import com.sanny_tech.carapp.databinding.DialogAddPayLinkBinding;
import com.sanny_tech.carapp.databinding.DialogTaxiImagesBinding;
import com.sanny_tech.carapp.databinding.FragmentDriverMainBinding;
import com.sanny_tech.carapp.entities.PayLink;
import com.sanny_tech.carapp.entities.Ride;
import com.sanny_tech.carapp.entities.SubscriptionPlan;
import com.sanny_tech.carapp.entities.TaxiLocation;
import com.sanny_tech.carapp.enums.CarActions;
import com.sanny_tech.carapp.guides.NewDriverGuideActivity;
import com.sanny_tech.carapp.services.LocationTrackService;
import com.sanny_tech.carapp.taxi_utils.DriverAvailabilityManager;
import com.sanny_tech.carapp.taxi_utils.OrientationManager;
import com.sanny_tech.carapp.taxi_utils.PaymentPlanActivity;
import com.sanny_tech.carapp.taxi_utils.SubscriptionManager;
import com.sanny_tech.carapp.taxi_utils.TaxisAvailable;
import com.sanny_tech.carapp.taxi_utils.TaxiInit;
import com.sanny_tech.carapp.taxi_utils.Trip;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.sanny_tech.carapp.taxi_utils.TripUtils;
import com.sanny_tech.carapp.utils.ImagePagerAdapter;
import com.sanny_tech.carapp.utils.IpAddressManager;
import com.sanny_tech.carapp.utils.RequestManager;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DriverMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriverMainFragment extends Fragment implements PaymentAdapter.OnItemClickListener,
        TripAdapter.OnItemClickListener, OrientationManager.OrientationListener,
        PaymentAdapter.OnItemLongClickListener {
    private static final int REQUEST_CODE = 4;
    private FragmentDriverMainBinding driverMainBinding;
    private FirebaseDatabase database;
    private DatabaseReference reference, payLinksReference;
    private double currentLongitude, currentLatitude;
    private FusedLocationProviderClient fusedLocationProviderClient;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TaxiLocation taxiLocation;
    private TripAdapter tripAdapter;
    private List<Trip> trips = new ArrayList<>();
    private int seat_count;
    private DriverAvailabilityManager availabilityManager;
    private FirebaseFirestore firestore;
    private PaymentAdapter paymentAdapter;
    private List<String> paymentMethods = new ArrayList<>();
    private PayLink payLink;
    private DialogAddPayLinkBinding linkBinding;
    private String selectedTransactionType;
    private float orientation = 0.0f;
    private OrientationManager orientationManager;
    private LocationTrackService locationTrackService;
    private String baseUrl;
    private SubscriptionManager subscriptionManager;
    private DatabaseReference availableRef;
    private SubscriptionPlan plan;
    private TaxiInit init;

    public DriverMainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DriverMainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DriverMainFragment newInstance(String param1, String param2) {
        DriverMainFragment fragment = new DriverMainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        driverMainBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_driver_main, container, false);
        if (getContext() != null) {
            baseUrl = IpAddressManager.getIpAddress(requireContext());
            database = FirebaseDatabase.getInstance();
            reference = database.getReference("taxi_locations");
            availableRef = FirebaseDatabase.getInstance().getReference("taxis");
            payLinksReference = database.getReference("pay_links");
            firestore = FirebaseFirestore.getInstance();
            subscriptionManager = new SubscriptionManager(requireContext());
            availabilityManager = new DriverAvailabilityManager(requireContext());
            orientationManager = new OrientationManager(requireContext(), this);
            orientationManager.startListening();
            locationTrackService = new LocationTrackService();
            loadPayLink();
            loadRide();
            loadActiveRequest();
            if (availabilityManager.getTaxiInit() == null) {
                Toast.makeText(requireContext(), "Loading data", Toast.LENGTH_SHORT).show();
                fetchUserProgress(getCurrentAccountId());
            }else {
                Log.d("init local","true");
                loadTaxiInit(availabilityManager.getTaxiInit());
                getRidesByDriverId(getCurrentAccountId());
            }
        }

        tripAdapter = new TripAdapter(trips, requireContext());
        tripAdapter.setOnItemClickListener(this);
        driverMainBinding.clientsRecycler.setAdapter(tripAdapter);
        driverMainBinding.clientsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        driverMainBinding.statusSwitch.setChecked(availabilityManager.getAvailabilityStatus());

        paymentMethods.add("Cash");
        paymentAdapter = new PaymentAdapter(paymentMethods, requireContext());
        paymentAdapter.setOnItemLongClickListener(this);
        paymentAdapter.setOnItemClickListener(this);
        driverMainBinding.paymentMethodsList.setAdapter(paymentAdapter);
        driverMainBinding.paymentMethodsList.setLayoutManager(new LinearLayoutManager(requireContext()));


        driverMainBinding.statusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateStatusText(isChecked);
            }
        });
        driverMainBinding.retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRidesByDriverId(getCurrentAccountId());
            }
        });
        driverMainBinding.fabPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPayLink();
            }
        });
        driverMainBinding.setUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), NewDriverGuideActivity.class);
                startActivity(intent);
            }
        });
        driverMainBinding.taxiDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isExpanded = driverMainBinding.activePlan.getVisibility() == View.VISIBLE;
                if (isExpanded) {
                    if (init != null && init.getTaxi_images() != null &&
                            !init.getTaxi_images().isEmpty()) {
                        showTaxiImagesDialog(init.getTaxi_images());
                    }else {
                        Toast.makeText(requireContext(), "No available images",
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(requireContext(), DriverDocsActivity.class);
                        intent.putExtra("init",init);
                        intent.putExtra("instruction","taxi_images");
                        startActivity(intent);
                    }
                }else {
                    toggleExpansion(driverMainBinding.activePlan, isExpanded);
                }
            }
        });
        driverMainBinding.activePlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (plan == null) {
                    Intent intent = new Intent(requireContext(), PaymentPlanActivity.class);
                    startActivity(intent);
                }else {
                    boolean isExpanded = driverMainBinding.activePlan.getVisibility() == View.VISIBLE;
                    toggleExpansion(driverMainBinding.activePlan, isExpanded);
                }
            }
        });
        driverMainBinding.docsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), DriverDocsActivity.class);
                intent.putExtra("init",init);
                startActivity(intent);
            }
        });
        driverMainBinding.allTripsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), DriverHistoryActivity.class);
                startActivity(intent);
            }
        });
        return driverMainBinding.getRoot();
    }

    private void glideImage(TaxiInit s) {
        if (getActivity() != null && s.getTaxi_images() != null &&
                s.getTaxi_images().size() != 0) {
            String endPoint = baseUrl + "/taxi/image/" + getCurrentAccountId() + "/"
                     + s.getTaxi_images().get(0);
            Log.e(DriverMainFragment.class.getSimpleName(), endPoint);
            Picasso.get()
                    .load(endPoint)
                    .into(driverMainBinding.taxiImage);
        }
    }

    private void loadActiveRequest() {
        DatabaseReference ridesReference = FirebaseDatabase.getInstance().getReference("taxi_rides");
        ridesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ride ride = snapshot.getValue(Ride.class);
                    if (ride != null && ride.getDriver_id().equals(getCurrentAccountId())) {
                        driverMainBinding.viewActiveRequestButton.setVisibility(View.VISIBLE);
                        driverMainBinding.viewActiveRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openDriverMaps(ride);
                            }
                        });
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

    private void openDriverMaps(Ride request) {
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
                        Intent intent = new Intent(requireContext(),
                                TaxiMapsActivity.class);
                        intent.putExtra("key", mapKey);
                        intent.putExtra("ride", request);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void showSnackbar(View rootView, String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.blue));
        snackbar.show();
    }

    private void updateStatusText(boolean isChecked) {
        if (isChecked) {
//            if (plan != null) {
//                long currentTime = System.currentTimeMillis();
//                long timeRemaining = plan.getExpiryDate() - currentTime;
//
//                if (timeRemaining > 0) {
                    driverMainBinding.statusLabel.setText("Currently: Available");
                    checkLocationServices();
//                } else {
//                    driverMainBinding.statusSwitch.setChecked(false);
//                    Intent intent = new Intent(requireContext(), PaymentPlanActivity.class);
//                    startActivity(intent);
//                }
//            }else {
//                Intent intent = new Intent(requireContext(), PaymentPlanActivity.class);
//                startActivity(intent);
//            }
        } else {
            driverMainBinding.statusLabel.setText("Currently: Unavailable");
            orientationManager.startListening();
            stopLocationService();
            deleteTaxiLocationFromFirebase();
        }
    }

    private void saveTaxiLocationToFirebase() {
        Toast.makeText(requireContext(), "Please wait, availing.", Toast.LENGTH_SHORT).show();
        // Get the driverId, longitude, and latitude
        String driverId = getCurrentAccountId(); // Replace with your driver's ID
        if (!checkAndRequestPermissions()) {
            return; // Permissions are not granted, exit the method
        }
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                currentLatitude = location.getLatitude();
                                currentLongitude = location.getLongitude();

                                taxiLocation = new TaxiLocation(driverId, seat_count, currentLongitude,
                                        currentLatitude, "available", orientation, null,
                                        availabilityManager.getTaxiInit());
                                if (payLink != null && payLink.getId() != null) {
                                    taxiLocation.setPayLink(payLink);
                                }
                                TaxisAvailable available = taxiLocation.createTaxiAvailble();
                                String category = taxiLocation.getTaxiInit().getCategory();
                                if (category.equals("Boda Boda")){
                                    category = "BodaBoda";
                                }
                                availableRef.child(taxiLocation.getStatus())
                                        .child(category)
                                        .child(taxiLocation.getTaxiInit().getTaxi_id())
                                        .setValue(available);
                                reference.child(driverId).setValue(taxiLocation);
                                availabilityManager.saveAvailabilityStatus(true);
                                startLocationService();
                            }
                        }
                    });
        // Create a TaxiLocation object

        // Save the TaxiLocation object to Firebase Realtime Database
    }
    private void checkLocationServices() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGpsEnabled && !isNetworkEnabled) {
            // Show a dialog to prompt the user to enable location services
            showLocationServicesDialog();
        } else {
            // Location services are enabled, proceed with getting the location
            saveTaxiLocationToFirebase();
        }
    }

    private void showLocationServicesDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Enable Location Services")
                .setMessage("Location services are required for this app. Please enable them in settings.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= 34) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.FOREGROUND_SERVICE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{
                        Manifest.permission.FOREGROUND_SERVICE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, REQUEST_CODE);
                return false;
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    private void startLocationService() {
        Intent serviceIntent = new Intent(requireContext(), LocationTrackService.class);
        requireContext().startService(serviceIntent);
    }

    private void stopLocationService() {
        Intent serviceIntent = new Intent(requireContext(), LocationTrackService.class);
        requireContext().stopService(serviceIntent);
    }

    private void loadRide() {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TaxiLocation taxiLocation1 = snapshot.getValue(TaxiLocation.class);
                    if (taxiLocation1 != null && taxiLocation1.getDriverId()
                            .equals(getCurrentAccountId())) {
                        taxiLocation = taxiLocation1;
                        driverMainBinding.statusSwitch.setChecked(true);
                        if (!availabilityManager.getAvailabilityStatus()) {
                            availabilityManager.saveAvailabilityStatus(true);
                        }
                    }else {
                        taxiLocation = null;
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
    private void deleteTaxiLocationFromFirebase() {

        // Get the driverId
        String driverId = getCurrentAccountId(); // Replace with your driver's ID

        // Delete the TaxiLocation object from Firebase Realtime Database
        reference.child(driverId).removeValue();
        if (taxiLocation != null) {
            availableRef.child("available")
                    .child(taxiLocation.getTaxiInit().getCategory())
                    .child(taxiLocation.getTaxiInit().getTaxi_id()).removeValue();
        }
        availabilityManager.saveAvailabilityStatus(false);

        DatabaseReference ridesReference = database.getReference("taxi_rides");
        ridesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Ride> rides = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ride ride = snapshot.getValue(Ride.class);
                    if (ride != null && ride.getDriver_id().equals(getCurrentAccountId())) {
                        RequestManager requestManager = new RequestManager(requireContext());
                        requestManager.clearRequest();
                        ridesReference.child(getCurrentAccountId()).removeValue();
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
        if (getContext() != null) {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
            return sharedPreferences.getString("currentUserId", null);
        }
        return null;
    }

    public String getCurrentPassword() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserPassword", null);
    }

    public String getCurrentEmail() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserEmail", null);
    }

    public String getCurrentAccountUserName() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserName", null);
    }

    public String getCurrentAccountType() {
        if (getContext() != null) {
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
            return sharedPreferences.getString("currentAccountType", null);
        }
        return null;
    }

    private void getRidesByDriverId(String driverId) {
        firestore.collection("trips")
                .whereEqualTo("driver_id", driverId)
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
                            TripUtils.DayTripsAndCharges dayTripsAndCharges =
                                    TripUtils.getTripsAndTotalCharges(receivedTrips);

                            List<Trip> filteredTrips = dayTripsAndCharges.getTrips();
                            double totalCharges = dayTripsAndCharges.getTotalCharges();
                            Locale kenyanLocale = new Locale("sw", "KE");
                            Currency kenyanShilling = Currency.getInstance("KES");
                            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(kenyanLocale);
                            numberFormat.setCurrency(kenyanShilling);
                            String formattedAmount = numberFormat.format(totalCharges);
                            driverMainBinding.timeText.setText(formattedAmount);
                            tripAdapter.setItems(filteredTrips);
                            hideProgressBar();
                            hideErrorLayout();
                        } else {
                            // No rides found for the given driver ID
                            hideProgressBar();
                            showErrorLayout();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        hideProgressBar();
                        showErrorLayout();
                    }
                });
    }

    public void fetchUserProgress(String userId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userProgressRef = database.getReference("drivers").child(userId);

        userProgressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Parse the user progress data
                    Map<String, Boolean> userProgress = new HashMap<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String step = snapshot.getKey();
                        Boolean isCompleted = snapshot.getValue(Boolean.class);
                        userProgress.put(step, isCompleted);
                    }
                    if (userProgress.size() != 0) {
                        driverMainBinding.mainLt.setVisibility(View.GONE);
                        driverMainBinding.docsButton.setVisibility(View.GONE);
                        driverMainBinding.progressBar.setVisibility(View.GONE);
                        driverMainBinding.rootLt.setVisibility(View.VISIBLE);
                        driverMainBinding.setUpButton.setVisibility(View.VISIBLE);
                    } else {
                        getAllTaxiInitForUser(getCurrentAccountId());
                    }
                } else {
                    getAllTaxiInitForUser(getCurrentAccountId());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getAllTaxiInitForUser(String userId) {
        List<TaxiInit> myInits = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("taxi_inits")
                .whereEqualTo("driver_id", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                TaxiInit taxiInit = document.toObject(TaxiInit.class);
                                myInits.add(taxiInit);
                                // Handle each TaxiInit object
                                Log.e("Firestore", "TaxiInit: " + taxiInit.toString());
                            }
                            if (myInits.get(0) != null) {
                                driverMainBinding.mainLt.setVisibility(View.VISIBLE);
                                loadTaxiInit(myInits.get(0));
//                                if (availabilityManager.getTaxiDetails().getTaxiImages() != null &&
//                                        !availabilityManager.getTaxiDetails().getTaxiImages().isEmpty()){
//                                    glideImage(availabilityManager.getTaxiDetails(),
//                                            myInits.get(0).getTaxi_id());
//                                }
                            } else {
                                driverMainBinding.mainLt.setVisibility(View.GONE);
                                driverMainBinding.docsButton.setVisibility(View.GONE);
                                driverMainBinding.progressBar.setVisibility(View.GONE);
                                driverMainBinding.rootLt.setVisibility(View.VISIBLE);
                                driverMainBinding.setUpButton.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.e("FireStore", "Not found");
                            driverMainBinding.mainLt.setVisibility(View.GONE);
                            driverMainBinding.docsButton.setVisibility(View.GONE);
                            driverMainBinding.progressBar.setVisibility(View.GONE);
                            driverMainBinding.rootLt.setVisibility(View.VISIBLE);
                            driverMainBinding.setUpButton.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error getting documents.", e);
                    }
                });
    }

    private void loadTaxiInit(TaxiInit taxiInit) {
        driverMainBinding.mainLt.setVisibility(View.VISIBLE);
        driverMainBinding.docsButton.setVisibility(View.VISIBLE);
        driverMainBinding.progressBar.setVisibility(View.GONE);
        driverMainBinding.rootLt.setVisibility(View.VISIBLE);
        init = taxiInit;
        fetchVerifiedDocs();
        driverMainBinding.statusText.setText(taxiInit.getCategory());
        driverMainBinding.plate.setText(taxiInit.getPlate_number());
        Log.d("plate",taxiInit.getPlate_number());
        driverMainBinding.desc.setText(MessageFormat.format("{0} {1}",
                taxiInit.getModel(), taxiInit.getColor()));
        glideImage(taxiInit);
        loadActivePlan();
    }

    private void loadActivePlan() {
        plan = subscriptionManager.getSubscriptionPlan();

        if (plan != null) {
            long currentTime = System.currentTimeMillis();
            long timeRemaining = plan.getExpiryDate() - currentTime;

            if (timeRemaining > 0) {
                String durationString = formatDuration(timeRemaining);
                driverMainBinding.plan.setText(MessageFormat.format("{0}\n{1}\nExpires in: {2}", plan.getName(), plan.getPrice(), durationString));
            } else {
                driverMainBinding.plan.setText("No active subscription plan.");
            }
        } else {
            driverMainBinding.plan.setText("No active subscription plan.");
        }
    }

    private void showErrorLayout() {
        driverMainBinding.errorLayout.setVisibility(View.VISIBLE);
    }

    private void hideErrorLayout() {
        driverMainBinding.errorLayout.setVisibility(View.GONE);
    }

    private void showProgressBar() {
        driverMainBinding.progressLt.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        driverMainBinding.progressLt.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadActiveRequest();

    }

    @Override
    public void onPause() {
        Log.e("driver", "paused");
        if (orientationManager != null) {
            orientationManager.stopListening();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (orientationManager != null) {
            orientationManager.stopListening();
        }
        super.onDestroy();
    }

    @Override
    public void onItemClick(String item) {

    }

    private void showAddPayLink() {
        final Dialog dialogView = new Dialog(requireContext());
        dialogView.requestWindowFeature(Window.FEATURE_NO_TITLE);
        linkBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.dialog_add_pay_link,
                null, false);
        dialogView.setContentView(linkBinding.getRoot());

        linkBinding.closeWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogView.dismiss();
            }
        });
        Spinner transaction_types = dialogView.findViewById(R.id.transaction_types_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.transaction_types, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transaction_types.setAdapter(new CustomSpinnerAdapter(requireContext(), R.layout.custom_spinner_dropdown_item,
                Arrays.asList(getResources().getTextArray(R.array.transaction_types))));


        transaction_types.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTransactionType = parent.getItemAtPosition(position).toString();
                showSnackbar(driverMainBinding.getRoot(), selectedTransactionType + " !");
                updateInputLayoutVisibility(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
                updateInputLayoutVisibility(0);
            }
        });

        linkBinding.sendMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String recipientNumber = linkBinding.recipientNumberEditText.getText().toString();

                if (recipientNumber.length() != 0) {
                    sendMoney(recipientNumber);
                }
            }
        });

        linkBinding.buyGoodsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shortcode = linkBinding.shortcodeEditText.getText().toString();

                if (!shortcode.isEmpty()) {
                    buyGoods(shortcode);
                }
            }
        });

        linkBinding.payBillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String payBillNumber = linkBinding.paybillEditText.getText().toString();
                String accountNumber = linkBinding.accountNumberEditText.getText().toString();

                if (payBillNumber.length() != 0 || accountNumber.length() != 0) {
                    payBill(payBillNumber, accountNumber);
                }
            }
        });

        linkBinding.pochiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pochiRecipient = linkBinding.pochiRecipientNumberEditText.getText().toString();

                if (pochiRecipient.length() != 0) {
                    pochiPay(pochiRecipient);
                }
            }
        });

        dialogView.show();
        dialogView.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogView.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogView.getWindow().getAttributes().windowAnimations = com.hbb20.R.style.Animation_AppCompat_Tooltip;
        dialogView.getWindow().setGravity(Gravity.CENTER);
    }

    private void pochiPay(String pochiRecipient) {
        payLink.setPochiRecipientNumber(pochiRecipient);
        savePayLink();
    }

    private void payBill(String payBillNumber, String accountNumber) {
        payLink.setPaybillNumber(payBillNumber);
        payLink.setAccountNumber(accountNumber);
        savePayLink();
    }

    private void buyGoods(String shortcode) {
        payLink.setShortcode(shortcode);
        savePayLink();
    }

    private void sendMoney(String recipientNumber) {
        payLink.setRecipientNumber(recipientNumber);
        savePayLink();
    }

    private void savePayLink() {
        showProgressBar();
        payLink.setId(getCurrentAccountId());
        payLinksReference.child(getCurrentAccountId()).setValue(payLink);
        Toast.makeText(requireContext(), "Pay-link created successfully", Toast.LENGTH_SHORT).show();
    }

    private void updateInputLayoutVisibility(int position) {
        linkBinding.sendMoneyLayout.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        linkBinding.buyGoodsLayout.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        linkBinding.payBillLayout.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
        linkBinding.pochiLayout.setVisibility(position == 3 ? View.VISIBLE : View.GONE);
//        checkBalanceLayout.setVisibility(position == 4 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(Trip item) {

    }

    @Override
    public Void onOrientationChanged(float azimuth) {
//        Log.e("azimuth", String.valueOf(azimuth));
        orientation = azimuth;
        return null;
    }

    private void loadPayLink() {
        payLinksReference.child(getCurrentAccountId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    payLink = dataSnapshot.getValue(PayLink.class);
                    // Do something with the payLink
                    updatePayLinks();
                } else {
                    // Handle the case where the paylink doesn't exist
                    Log.e("driver fragment", "not found");
                    payLink = new PayLink();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors
                Log.e("driver fragment", databaseError.getDetails());
            }
        });

    }

    private void updatePayLinks() {
        List<String> methodList = new ArrayList<>();
        methodList.add("Cash");
        if (payLink != null && payLink.getId() != null) {
            if (payLink.getShortcode() != null) {
                methodList.add("Buy goods and services");
            } else if (payLink.getPaybillNumber() != null) {
                methodList.add("Pay-bill");
            } else if (payLink.getPochiRecipientNumber() != null) {
                methodList.add("Pochi la biashara");
            } else if (payLink.getRecipientNumber() != null) {
                methodList.add("Send Money");
            }
            paymentAdapter.setItems(methodList);
        }
    }

    @Override
    public void onItemLongClick(String item) {
        showDeletePrompt(item);
    }

    private void showDeletePrompt(String item) {
        // Create and configure an AlertDialog or DialogFragment
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Abiri Africa");
        builder.setMessage("Are you sure you want to delete this pay-link (" + item + "). " +
                "This action is irreversible.");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (item.matches("Send Money")) {
                    payLink.setRecipientNumber(null);
                } else if (item.matches("Buy goods and services")) {
                    payLink.setShortcode(null);
                } else if (item.matches("Pay-bill")) {
                    payLink.setPaybillNumber(null);
                } else if (item.matches("Pochi la biashara")) {
                    payLink.setPochiRecipientNumber(null);
                }
                savePayLink();
            }
        });

        builder.show();
    }

    private String formatDuration(long durationMillis) {
        long days = TimeUnit.MILLISECONDS.toDays(durationMillis);
        durationMillis -= TimeUnit.DAYS.toMillis(days);

        long hours = TimeUnit.MILLISECONDS.toHours(durationMillis);
        durationMillis -= TimeUnit.HOURS.toMillis(hours);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis);
        durationMillis -= TimeUnit.MINUTES.toMillis(minutes);

        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis);

        StringBuilder durationString = new StringBuilder();
        if (days > 0) {
            durationString.append(days).append(" days ");
        }
        if (hours > 0) {
            durationString.append(hours).append(" hours ");
        }
        if (minutes > 0) {
            durationString.append(minutes).append(" minutes ");
        }
        if (seconds > 0) {
            durationString.append(seconds).append(" seconds");
        }

        return durationString.toString().trim();
    }

    private void toggleExpansion(View expandLayout, boolean isExpanded) {
        if (isExpanded) {
            expandLayout.setVisibility(View.GONE);
            ObjectAnimator.ofFloat(expandLayout, "scaleY", 1f, 0f)
                    .setDuration(300)
                    .start();
        } else {
            expandLayout.setVisibility(View.VISIBLE);
            ObjectAnimator.ofFloat(expandLayout, "scaleY", 0f, 1f)
                    .setDuration(300)
                    .start();
        }
    }
    private void fetchVerifiedDocs() {
        FunctionsLoader functionsLoader1 = new FunctionsLoader(requireContext(),
                CarActions.FETCH_VERIFIED_DOCS,
                getCurrentAccountId(),null);
        functionsLoader1.forceLoad();
        functionsLoader1.registerListener(5, new Loader.OnLoadCompleteListener<ArrayList<String>>() {
            @Override
            public void onLoadComplete(@NonNull Loader<ArrayList<String>> loader, @Nullable ArrayList<String> data) {
                if (data != null){
                    Log.d("verified docs",data.toString());
                    if (!data.contains("national_id") || !data.contains("driving_license")){
                        showSnackbarDocs(driverMainBinding.getRoot(),
                                "National id and driving license are compulsory");
                        driverMainBinding.mainLt.setVisibility(View.GONE);
                        driverMainBinding.setUpButton.setVisibility(View.GONE);
                    }
                }
            }
        });
    }
    private void showSnackbarDocs(View rootView, String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.red));
        snackbar.setAction("Upload", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), DriverDocsActivity.class);
                intent.putExtra("init",init);
                startActivity(intent);
            }
        });
        snackbar.show();
    }
    private void showTaxiImagesDialog(List<String> taxiImages) {
        Dialog dialog = new Dialog(requireContext());
        DialogTaxiImagesBinding taxiImagesBinding = DataBindingUtil.inflate(
                getLayoutInflater(),R.layout.dialog_taxi_images, null,false);
        dialog.setContentView(taxiImagesBinding.getRoot());

        // Example taxi images URLs
        ArrayList<String> imageUrls = new ArrayList<>();
        for (String image : taxiImages){
            String endPoint = baseUrl + "/taxi/image/" + getCurrentAccountId() + "/"
                    + image;
            imageUrls.add(endPoint);
        }

        ImagePagerAdapter adapter = new ImagePagerAdapter(requireContext(), imageUrls);
        taxiImagesBinding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(taxiImagesBinding.tabLayout, taxiImagesBinding.viewPager,
                (tab, position) -> {}).attach();
        taxiImagesBinding.uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), DriverDocsActivity.class);
                intent.putExtra("init",init);
                intent.putExtra("instruction","taxi_images");
                startActivity(intent);
            }
        });

        dialog.show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                // Permissions granted, start the foreground service
                checkLocationServices();
            } else {
                // Permissions denied, handle appropriately
                Toast.makeText(locationTrackService,
                        "Grant the application all necessary permissions to avail taxi",
                        Toast.LENGTH_SHORT).show();
                if (driverMainBinding.statusSwitch.isChecked()){
                    driverMainBinding.statusSwitch.setChecked(false);
                }
            }
        }
    }

}