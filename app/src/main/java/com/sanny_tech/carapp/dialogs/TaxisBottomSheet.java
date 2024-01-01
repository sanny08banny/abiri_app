package com.sanny_tech.carapp.dialogs;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.SignInActivity;
import com.sanny_tech.carapp.adapters.TaxiAdapter;
import com.sanny_tech.carapp.asynctasks.TaxiLoader;
import com.sanny_tech.carapp.databinding.TaxisLtBinding;
import com.sanny_tech.carapp.entities.Ride;
import com.sanny_tech.carapp.entities.TaxiLocation;
import com.sanny_tech.carapp.enums.ActionType;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class TaxisBottomSheet extends BottomSheetDialogFragment implements TaxiAdapter.OnItemClickListener {
    private TaxisLtBinding taxisLtBinding;
    private List<TaxiLocation> taxiLocations;
    private String string, distance;
    private double currentLatitude, currentLongitude, travelDistance;
    private float dest_lat, dest_lon;
    private TaxiBookingListener bookingListener;
    private DatabaseReference ridesReference;
    private FirebaseDatabase database;
    private TaxiLocation taxiLocation;
    private Ride ride;

    public TaxisBottomSheet(List<TaxiLocation> taxiLocations, String string, String distance, double currentLatitude, double currentLongitude, double travelDistance, float destLat, float destLon) {
        this.taxiLocations = taxiLocations;
        this.string = string;
        this.distance = distance;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.travelDistance = travelDistance;
        dest_lat = destLat;
        dest_lon = destLon;
    }

    public interface TaxiBookingListener {
        void onBookingResponse(boolean isSuccess, TaxiLocation item);
    }

    public void setBookingListener(TaxiBookingListener listener) {
        this.bookingListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        taxisLtBinding = DataBindingUtil.inflate(inflater, R.layout.taxis_lt, container, false);

        taxisLtBinding.selectedLoc.setText(string);
        taxisLtBinding.distance.setText(distance);
        Log.d("Bottom sheet", String.valueOf(taxiLocations.size()));
        database = FirebaseDatabase.getInstance();
        ridesReference = database.getReference("taxi_rides");
        TaxiAdapter taxiAdapter = new TaxiAdapter(taxiLocations, requireContext(),
                currentLatitude, currentLongitude, travelDistance);
        taxisLtBinding.taxisRecycler.setAdapter(taxiAdapter);
        taxisLtBinding.taxisRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        taxiAdapter.setOnItemClickListener(this);

        taxisLtBinding.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return taxisLtBinding.getRoot();
    }

    @Override
    public void onItemClick(TaxiLocation item) {
        taxiLocation = item;
        loadRide(item);
    }
    private void makeRequest(TaxiLocation item) {
        if (getCurrentAccountId() != null) {
            TaxiLoader taxiLoader = new TaxiLoader(requireContext(), item.getDriverId(),
                    dest_lat, dest_lon, (float) currentLatitude, (float) currentLongitude, ActionType.BOOK);
            taxiLoader.forceLoad();
            taxiLoader.registerListener(4, new Loader.OnLoadCompleteListener<String>() {
                @Override
                public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                    boolean isSuccess = (data != null);
                    if (data != null) {
                        Toast.makeText(requireContext(), "Success, wait for response", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show();
                    }
                    // Notify the activity about the booking response
                    if (bookingListener != null) {
                        bookingListener.onBookingResponse(isSuccess,item);
                    }
                }
            });
        }else {
            Toast.makeText(requireContext(),
                            "You must have an account",
                            Toast.LENGTH_LONG)
                    .show();
        }
    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs",
                MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
    private void showSnackbar(View rootView, String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.blue));
        snackbar.setAction("Sign in", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), SignInActivity.class);
                startActivity(intent);
            }
        });
        snackbar.setAction("Cancel", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }
    private void loadRide(TaxiLocation item) {
        Log.e("Taxi", "loading");
        ridesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e("Taxi", "snap found");
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.e("Taxi", "snap found");
                    ride = snapshot.getValue(Ride.class);
                }
                // You can pass this list to your UI or perform further operations
                if (ride != null && ride.getUser_id().equals(getCurrentAccountId())) {
                    Log.e("Taxi", "loading");
                    if (bookingListener != null) {
                        bookingListener.onBookingResponse(true,item);
                    }
                }else {
                    Log.e("Taxi", "unfound");
                    makeRequest(item);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
