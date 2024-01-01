package com.sanny_tech.carapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.TripAdapter;
import com.sanny_tech.carapp.databinding.ActivityMyTripsBinding;
import com.sanny_tech.carapp.taxi_utils.Trip;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyTripsActivity extends AppCompatActivity {
    private TripAdapter tripAdapter;
    private ActivityMyTripsBinding myTripsBinding;
    private FirebaseFirestore firestore;
    private List<Trip> trips = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myTripsBinding = DataBindingUtil.setContentView(this,R.layout.activity_my_trips);
        setSupportActionBar(myTripsBinding.toolbar);
        myTripsBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        firestore = FirebaseFirestore.getInstance();

        tripAdapter = new TripAdapter(trips,this);
        myTripsBinding.myTrips.setAdapter(tripAdapter);
        myTripsBinding.myTrips.setLayoutManager(new LinearLayoutManager(this));

        getRidesByDriverId(getCurrentAccountId());
    }
    private void getRidesByDriverId(String driverId) {
        firestore.collection("trips")
                .whereEqualTo("user_id", driverId)
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
                            hideErrorLayout();
                        } else {
                            // No rides found for the given driver ID
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
        myTripsBinding.errorLayout.setVisibility(View.VISIBLE);
    }

    private void hideErrorLayout() {
        myTripsBinding.errorLayout.setVisibility(View.GONE);
    }
    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
}