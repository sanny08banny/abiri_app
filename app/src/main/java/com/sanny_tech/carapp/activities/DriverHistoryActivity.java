package com.sanny_tech.carapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.databinding.ActivityDriverHistoryBinding;
import com.sanny_tech.carapp.taxi_utils.Trip;
import com.sanny_tech.carapp.viewPagers.TripsPagerAdapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DriverHistoryActivity extends AppCompatActivity {
    private ActivityDriverHistoryBinding binding;
    private TripsPagerAdapter pagerAdapter;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_driver_history);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        firestore = FirebaseFirestore.getInstance();
        getRidesByDriverId(getCurrentAccountId());
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
                            setUpViewPager(receivedTrips);

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

    private void setUpViewPager(List<Trip> trips) {
        // Sort trips by start_time
        Collections.sort(trips, new Comparator<Trip>() {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            @Override
            public int compare(Trip trip1, Trip trip2) {
                try {
                    Date startTime1 = format.parse(trip1.getStart_time());
                    Date startTime2 = format.parse(trip2.getStart_time());
                    return startTime1.compareTo(startTime2);
                } catch (ParseException e) {
                    e.printStackTrace(); // Handle parse exception as needed
                }
                return 0;
            }
        });

        // Group trips by day
        Map<String, List<Trip>> tripsByDay = new HashMap<>();
        for (Trip trip : trips) {
            String day = trip.getStart_time().substring(0, 10); // Extract yyyy-MM-dd part
            if (!tripsByDay.containsKey(day)) {
                tripsByDay.put(day, new ArrayList<>());
            }
            tripsByDay.get(day).add(trip);
        }
        // Create adapter for ViewPager2
        pagerAdapter = new TripsPagerAdapter(this, tripsByDay);
        binding.viewPager.setAdapter(pagerAdapter);

        // Link ViewPager2 with TabLayout
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(formatTime(Long.parseLong(pagerAdapter.getTitle(position))))
        ).attach();
    }
    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    private void showErrorLayout() {
        binding.errorLayout.setVisibility(View.VISIBLE);
    }

    private void hideErrorLayout() {
        binding.errorLayout.setVisibility(View.GONE);
    }

    private void showProgressBar() {
        binding.progressLt.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        binding.progressLt.setVisibility(View.GONE);
    }
    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
    public String getCurrentPassword() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserPassword", null);
    }
    public String getCurrentEmail() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserEmail", null);
    }
    public String getCurrentAccountUserName() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserName", null);
    }
    public String getCurrentAccountType() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentAccountType", null);
    }
}