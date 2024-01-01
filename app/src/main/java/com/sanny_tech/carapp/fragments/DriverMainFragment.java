package com.sanny_tech.carapp.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.TaxiMapsActivity;
import com.sanny_tech.carapp.adapters.CustomSpinnerAdapter;
import com.sanny_tech.carapp.adapters.PaymentAdapter;
import com.sanny_tech.carapp.adapters.TripAdapter;
import com.sanny_tech.carapp.databinding.DialogAddPayLinkBinding;
import com.sanny_tech.carapp.databinding.FragmentDriverMainBinding;
import com.sanny_tech.carapp.entities.PayLink;
import com.sanny_tech.carapp.entities.TaxiLocation;
import com.sanny_tech.carapp.taxi_utils.ClientRequest;
import com.sanny_tech.carapp.taxi_utils.DriverAvailabilityManager;
import com.sanny_tech.carapp.taxi_utils.Trip;
import com.sanny_tech.carapp.utils.RequestManager;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DriverMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriverMainFragment extends Fragment implements PaymentAdapter.OnItemClickListener,
TripAdapter.OnItemClickListener{
    private static final int REQUEST_CODE = 4;
    private FragmentDriverMainBinding driverMainBinding;
    private FirebaseDatabase database;
    private DatabaseReference reference;
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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        driverMainBinding = DataBindingUtil.inflate(
                inflater,R.layout.fragment_driver_main, container, false);

        payLink = new PayLink();
        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("taxi_locations");
        firestore = FirebaseFirestore.getInstance();
        availabilityManager = new DriverAvailabilityManager(requireContext());
        loadRide();

        tripAdapter = new TripAdapter(trips,requireContext());
        tripAdapter.setOnItemClickListener(this);
        driverMainBinding.clientsRecycler.setAdapter(tripAdapter);
        driverMainBinding.clientsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        seat_count = availabilityManager.getSeatCount();

        updateSeatCount();
        driverMainBinding.statusSwitch.setChecked(availabilityManager.getAvailabilityStatus());

        paymentMethods.add("Cash");
        paymentAdapter = new PaymentAdapter(paymentMethods,requireContext());
        paymentAdapter.setOnItemClickListener(this);
        driverMainBinding.paymentMethodsList.setAdapter(paymentAdapter);
        driverMainBinding.paymentMethodsList.setLayoutManager(new LinearLayoutManager(requireContext()));

        driverMainBinding.statusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateStatusText(isChecked);
            }
        });
        getRidesByDriverId(getCurrentAccountId());

        loadActiveRequest();

        driverMainBinding.addSeats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seat_count = seat_count + 1;
                updateSeatCount();
            }
        });

        driverMainBinding.minusSeats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newQuantity = seat_count - 1;
                if (newQuantity >= 1) {
                    seat_count = newQuantity;
                    updateSeatCount();
                } else {
                    showSnackbar(driverMainBinding.getRoot(),"Seats cannot be empty");
                }
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
        return driverMainBinding.getRoot();
    }

    private void loadActiveRequest() {
        List<ClientRequest> requests = RequestManager.loadRequest(requireContext());
        if (requests != null && requests.size() > 0){
            driverMainBinding.viewActiveRequestButton.setVisibility(View.VISIBLE);
            driverMainBinding.viewActiveRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDriverMaps(requests.get(0));
                }
            });
        }

    }
    private void openDriverMaps(ClientRequest request) {
        Intent intent = new Intent(requireContext(), TaxiMapsActivity.class);
        intent.putExtra("request", request);
        startActivity(intent);
    }
    private void updateSeatCount() {
        driverMainBinding.seatCount.setText(String.valueOf(seat_count));
        availabilityManager.saveSeatCount(seat_count);
    }

    private void showSnackbar(View rootView, String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.blue));
        snackbar.show();
    }
    private void updateStatusText(boolean isChecked) {
        if (isChecked) {
            driverMainBinding.statusText.setText("Currently: Available");
            saveTaxiLocationToFirebase();
        } else {
            driverMainBinding.statusText.setText("Currently: Unavailable");
            deleteTaxiLocationFromFirebase();
        }
    }
    private void saveTaxiLocationToFirebase() {
        // Get the driverId, longitude, and latitude
        String driverId = getCurrentAccountId(); // Replace with your driver's ID
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
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
                                    currentLatitude,"available");
                            reference.child(driverId).setValue(taxiLocation);
                            availabilityManager.saveAvailabilityStatus(true);

                        }
                    }
                });

        // Create a TaxiLocation object

        // Save the TaxiLocation object to Firebase Realtime Database
    }
    private void loadRide() {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TaxiLocation taxiLocation1 = snapshot.getValue(TaxiLocation.class);
                    if (taxiLocation1 != null && taxiLocation1.getDriverId().equals(getCurrentAccountId())) {
                        if (!availabilityManager.getAvailabilityStatus()){
                            availabilityManager.saveAvailabilityStatus(true);
                            availabilityManager.saveSeatCount((int) taxiLocation1.getSeats());
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


    private void deleteTaxiLocationFromFirebase() {
        // Get the driverId
        String driverId = getCurrentAccountId(); // Replace with your driver's ID

        // Delete the TaxiLocation object from Firebase Realtime Database
        reference.child(driverId).removeValue();
        availabilityManager.saveAvailabilityStatus(false);
    }
    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
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
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserName", null);
    }
    public String getCurrentAccountType() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentAccountType", null);
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
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                                Trip trip = documentSnapshot.toObject(Trip.class);
                                receivedTrips.add(trip);
                            }
                            tripAdapter.setItems(receivedTrips);
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
                    }
                });
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
                payLink.setTransactionType(selectedTransactionType);
                showSnackbar(driverMainBinding.getRoot(),selectedTransactionType + " !");
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

                if (shortcode.length() != 0) {
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
}