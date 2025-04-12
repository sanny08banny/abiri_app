package com.sanny_tech.carapp.taxi_utils;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.communication_utils.EmailSender;
import com.sanny_tech.carapp.communication_utils.SmsSender;
import com.sanny_tech.carapp.databinding.ActivityTripBinding;
import com.sanny_tech.carapp.hire_utils.Hire;
import com.sanny_tech.carapp.hire_utils.HireActivity;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

public class TripActivity extends AppCompatActivity {
    private ActivityTripBinding binding;
    private DatabaseReference tripRef;
    private Trip currentTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_trip);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        String tripId = getIntent().getStringExtra("tripId");
        String hireId = getIntent().getStringExtra("hireId");


        if (tripId != null) {
            // Firebase reference to the specific trip
            tripRef = FirebaseDatabase.getInstance().getReference("trips").child(tripId);
//            SmsSender.initializeTwilio();
            // Listen for changes in trip details
            tripRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    currentTrip = dataSnapshot.getValue(Trip.class);
                    if (currentTrip != null && !currentTrip.getEnd_time().isEmpty()) {
                        // Trip is complete, prompt driver for payment confirmation

                        binding.tripDuration.setText(getFormattedElapsedTime(
                                Long.parseLong(currentTrip.getEnd_time()) -
                                        Long.parseLong(currentTrip.getStart_time())
                        ));
                        binding.textTripId.setText("#" + currentTrip.getId());
                        Locale kenyanLocale = new Locale("sw", "KE");
                        Currency kenyanShilling = Currency.getInstance("KES");
                        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(kenyanLocale);
                        numberFormat.setCurrency(kenyanShilling);
                        String formattedAmount = numberFormat.format(
                                Double.parseDouble(currentTrip.getCharges()));
                        binding.textCharges.setText(formattedAmount);
                        binding.textPickUp.setText(" " + currentTrip.getPick_up());
                        binding.textDestination.setText(" " + currentTrip.getDestination());

                        // Check if trip is complete and show payment confirmation UI
                        if (!currentTrip.getEnd_time().equals("")) {
                            binding.confirmPaymentLayout.setVisibility(View.VISIBLE);
                        } else {
                            binding.confirmPaymentLayout.setVisibility(View.GONE);
                        }
                        binding.confirmButtonf.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mergeTripToFirestore(currentTrip);
                                finish();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(TripActivity.this, "Error fetching trip details", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (hireId != null) {
            loadHire(hireId);
        }
    }
    public String getFormattedElapsedTime(long elapsedMillis) {
        long seconds = elapsedMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        // Format the elapsed time into a readable string
        String formattedTime = String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
        return formattedTime;
    }
    private void loadHire(String hireId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("hires");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            Hire hire;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Hire activeHire;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    activeHire = snapshot.getValue(Hire.class);
                    if (activeHire != null && activeHire.getId().equals(hireId)) {
                        hire = activeHire;
                    }
                }
                if (hire != null && hire.getId().equals(hireId)) {
                    binding.tripDuration.setText(getFormattedElapsedTime(
                            Long.parseLong(hire.getEnd_date()) -
                                    Long.parseLong(hire.getStart_date())
                    ));
                    binding.textTripId.setText("#" + hire.getId());
                    Locale kenyanLocale = new Locale("sw", "KE");
                    Currency kenyanShilling = Currency.getInstance("KES");
                    NumberFormat numberFormat = NumberFormat.getCurrencyInstance(kenyanLocale);
                    numberFormat.setCurrency(kenyanShilling);
                    String formattedAmount = numberFormat.format(hire.getCharges());
                    binding.textCharges.setText(formattedAmount);
                    binding.textPickUp.setText(formatTime(Long.parseLong(hire.getStart_date())));
                    binding.textDestination.setText(formatTime(Long.parseLong(hire.getEnd_date())));

                    // Check if trip is complete and show payment confirmation UI
                    if (!hire.getStatus().equals("complete")) {
                        binding.confirmPaymentLayout.setVisibility(View.VISIBLE);
                    } else {
                        binding.confirmPaymentLayout.setVisibility(View.GONE);
                    }
                    binding.confirmButtonf.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hire.setStatus("complete");
                            reference.child(hireId).setValue(hire);
                            String userPhoneNumber = hire.getClient_contact(); // Replace with actual user phone number
//                            SmsSender.sendSms(userPhoneNumber, "Your hire receipt: \n" + hire.generateReceipt());
                            finish();
                        }
                    });
                }else {
                    Toast.makeText(TripActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
    }
    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    public void mergeTripToFirestore(Trip ride) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("trips")
                .document(ride.getId())
                .set(ride, SetOptions.merge())
                .addOnSuccessListener(documentReference -> {
                    // Successfully merged the ride into Firestore
                    // Handle success, if needed
                })
                .addOnFailureListener(e -> {
                    // Failed to merge the ride into Firestore
                    // Handle failure, if needed
                });
    }

}