package com.sanny_tech.carapp.dialogs;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
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

import com.google.android.gms.maps.model.LatLng;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.SignInActivity;
import com.sanny_tech.carapp.adapters.TaxiAdapter;
import com.sanny_tech.carapp.asynctasks.TaxiLoader;
import com.sanny_tech.carapp.databinding.TaxisLtBinding;
import com.sanny_tech.carapp.entities.Ride;
import com.sanny_tech.carapp.entities.TaxiCategory;
import com.sanny_tech.carapp.entities.TaxiLocation;
import com.sanny_tech.carapp.enums.ActionType;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sanny_tech.carapp.taxi_utils.FareCalculator;
import com.sanny_tech.carapp.taxi_utils.PricingDetails;
import com.sanny_tech.carapp.taxi_utils.RouteCalculator;
import com.sanny_tech.carapp.taxi_utils.TaxiLocationGrouper;
import com.sanny_tech.carapp.taxi_utils.TaxiPrice;
import com.sanny_tech.carapp.taxi_utils.TravelDetails;
import com.sanny_tech.carapp.taxi_utils.Vehicle;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private Context context;
    private Vehicle selectedItem;
    private PricingDetails pricingDetails;
    private Double charges;
    private String dest_name,pick_up_name;

    public TaxisBottomSheet(List<TaxiLocation> taxiLocations, String string,
                            String distance, double currentLatitude, double currentLongitude,
                            double travelDistance, float destLat, float destLon, Context context,
                            String destName, String pickUpName) {
        this.taxiLocations = taxiLocations;
        this.string = string;
        this.distance = distance;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.travelDistance = travelDistance;
        dest_lat = destLat;
        dest_lon = destLon;
        this.context = context;
        dest_name = destName;
        pick_up_name = pickUpName;
    }

    public interface TaxiBookingListener {
        void onBookingResponse(boolean isSuccess, Vehicle item);
    }

    public void setBookingListener(TaxiBookingListener listener) {
        this.bookingListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        taxisLtBinding = DataBindingUtil.inflate(inflater, R.layout.taxis_lt, container, false);

        taxisLtBinding.selectedLoc.setText(pick_up_name + " to " + string);
        taxisLtBinding.distance.setText(distance);
        Log.d("Bottom sheet", String.valueOf(taxiLocations.size()));
        database = FirebaseDatabase.getInstance();
        ridesReference = database.getReference("taxi_rides");
        FareCalculator fareCalculator = new FareCalculator(context);
        pricingDetails = new PricingDetails(getCurrentAccountId(),
                currentLatitude,currentLongitude,dest_lat,dest_lon);
        fareCalculator.calculateFare(pricingDetails, new FareCalculator.FareCallback() {
            @Override
            public void onSuccess(TaxiPrice taxiPrice) {
                List<Vehicle> vehicles = new ArrayList<>();
                Map<String, List<TaxiLocation>> groupedTaxis = TaxiLocationGrouper.groupBySeatCount(taxiLocations);

                Map<String, Double> categoryPrices = new HashMap<>();
                categoryPrices.put("Economy", taxiPrice.getEconomy());
                categoryPrices.put("Classic", taxiPrice.getClassic());
                categoryPrices.put("Xl", taxiPrice.getXl());
                categoryPrices.put("BodaBoda", taxiPrice.getBodaBoda());

                for (Map.Entry<String, Double> entry : categoryPrices.entrySet()) {
                    String category = entry.getKey();
                    double price = entry.getValue();
                    Vehicle vehicle = new Vehicle(category, TaxiCategory.getNumberOfSeats(category)); // Initial seat count is 0
                    vehicle.setTaxiLocations(new ArrayList<>()); // Empty list of TaxiLocations
                    vehicle.setPrice(price);
                    vehicles.add(vehicle);
                }

                for (Map.Entry<String, List<TaxiLocation>> entry : groupedTaxis.entrySet()) {
                    String category = entry.getKey();
                    List<TaxiLocation> locations = entry.getValue();
                    for (Vehicle vehicle : vehicles) {
                        if (vehicle.getCategory().equals(category)) {
                            if (!locations.isEmpty()) {
                                vehicle.setSeat_count(TaxiCategory.getNumberOfSeats(vehicle.getCategory()));
                                vehicle.setTaxiLocations(locations);
                            }
                            break;
                        }
                    }
                }

                TaxiAdapter taxiAdapter = new TaxiAdapter(vehicles, context, currentLatitude, currentLongitude, travelDistance);
                taxisLtBinding.taxisRecycler.setAdapter(taxiAdapter);
                taxisLtBinding.taxisRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
                taxiAdapter.setOnItemClickListener(TaxisBottomSheet.this);

                if (vehicles.isEmpty()){
                    taxisLtBinding.noTaxisLt.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(context, "Not loaded", Toast.LENGTH_SHORT).show();
            }
        });

        taxisLtBinding.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        taxisLtBinding.requestRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedItem != null){
                    taxisLtBinding.mainLt.setVisibility(View.GONE);
                    taxisLtBinding.confirmLt.setVisibility(View.VISIBLE);
                    taxisLtBinding.confirm.setVisibility(View.GONE);
                    taxisLtBinding.category.setText(selectedItem.getCategory());
                    charges = selectedItem.getPrice();
                    Locale kenyanLocale = new Locale("sw", "KE");
                    Currency kenyanShilling = Currency.getInstance("KES");
                    NumberFormat numberFormat = NumberFormat.getCurrencyInstance(kenyanLocale);
                    numberFormat.setCurrency(kenyanShilling);
                    String formattedAmount = numberFormat.format(charges);
                    RouteCalculator routeCalculator = new RouteCalculator(context);
                    routeCalculator.calculateSingleTravelDetails(new LatLng(currentLatitude,
                            currentLongitude), new LatLng(dest_lat, dest_lon), new RouteCalculator.SingleTravelDetailsCallback() {
                        @Override
                        public void onSingleTravelDetailsCalculated(TravelDetails travelDetails) {
                            taxisLtBinding.duration.setText("( " + travelDetails.getReadableDuration() + " ) " +
                                    travelDetails.getReadableDistance() + " away");
                            taxisLtBinding.price.setText(formattedAmount);
                            taxisLtBinding.confirm.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }
                    });
                }else {
                    Toast.makeText(context, "No ride selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        taxisLtBinding.confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRide(selectedItem);
            }
        });
        taxisLtBinding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taxisLtBinding.mainLt.setVisibility(View.VISIBLE);
                taxisLtBinding.confirmLt.setVisibility(View.GONE);
            }
        });
        return taxisLtBinding.getRoot();
    }

    @Override
    public void onItemClick(Vehicle item) {
        taxisLtBinding.requestRide.setVisibility(View.VISIBLE);
        Toast.makeText(context, item.getCategory() + " selected", Toast.LENGTH_SHORT).show();
        selectedItem = item;
    }

    private void makeRequest(Vehicle item) {
        if (getCurrentAccountId() != null) {
            ArrayList<String> driverIds = new ArrayList<>();
            for (TaxiLocation location : item.getTaxiLocations()) {
                driverIds.add(location.getDriverId());
            }
            taxisLtBinding.confirm.setVisibility(View.GONE);
            TaxiLoader taxiLoader = new TaxiLoader(context,charges,pricingDetails, ActionType.BOOK,
                    dest_name,null, item.getCategory());
            taxiLoader.forceLoad();
            taxiLoader.registerListener(4, new Loader.OnLoadCompleteListener<String>() {
                @Override
                public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                    boolean isSuccess = (data != null);
                    if (data != null) {
                        Toast.makeText(context, "Success, wait for response", Toast.LENGTH_SHORT).show();
                    } else {
                        taxisLtBinding.confirm.setVisibility(View.VISIBLE);
                        taxisLtBinding.confirm.setText("Retry");
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                        taxiLoader.abandon();
                    }
                    // Notify the activity about the booking response
                    if (bookingListener != null) {
                        bookingListener.onBookingResponse(isSuccess, item);
                    }
                }
            });
        } else {
            Toast.makeText(context,
                            "You must have an account",
                            Toast.LENGTH_LONG)
                    .show();
        }
    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AccountPrefs",
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

    private void loadRide(Vehicle item) {
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
                        bookingListener.onBookingResponse(true, item);
                    }
                } else {
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
