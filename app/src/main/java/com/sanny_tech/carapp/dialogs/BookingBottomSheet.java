package com.sanny_tech.carapp.dialogs;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.loader.content.Loader;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.SignInActivity;
import com.sanny_tech.carapp.adapters.TaxiAdapter;
import com.sanny_tech.carapp.asynctasks.BookCarLoader;
import com.sanny_tech.carapp.databasehelpers.UploadedCarsHelper;
import com.sanny_tech.carapp.databinding.BookingDialogLtBinding;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.entities.CarBookRequest;
import com.sanny_tech.carapp.entities.NewBookingRequest;
import com.sanny_tech.carapp.entities.TaxiLocation;
import com.sanny_tech.carapp.enums.ActionType;
import com.sanny_tech.carapp.hire_utils.Hire;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sanny_tech.carapp.taxi_utils.Vehicle;
import com.sanny_tech.carapp.utils.SimCardManager;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookingBottomSheet extends BottomSheetDialogFragment implements TaxiAdapter.OnItemClickListener {
    private BookingDialogLtBinding bookingDialogLtBinding;
    private CarBookRequest carBookRequest;
    private TaxiBookingListener bookingListener;
    private DatabaseReference reference;
    private FirebaseDatabase database;
    private Hire activeHire;
    private NewBookingRequest bookingRequest;

    public BookingBottomSheet(CarBookRequest carBookRequest) {
        this.carBookRequest = carBookRequest;
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
        bookingDialogLtBinding = DataBindingUtil.inflate(inflater, R.layout.booking_dialog_lt, container, false);

        setCancelable(false);

        bookingDialogLtBinding.userName.setText(MessageFormat.format(
                "{0} is requesting to hire your car. The requested car is {1}",
                carBookRequest.getUser_name(), carBookRequest.getCar_id()));
        bookingDialogLtBinding.userPhone.setText(MessageFormat.format("Clients number: {0}",
                carBookRequest.getUser_phone()));

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("hires");
        loadHire(carBookRequest);
        bookingDialogLtBinding.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        
        bookingDialogLtBinding.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptBookingRequest(carBookRequest);
            }
        });
        bookingDialogLtBinding.decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                declineBookingRequest(carBookRequest);
            }
        });
        return bookingDialogLtBinding.getRoot();
    }

    private void acceptBookingRequest(CarBookRequest carBookRequest) {
        showProgressBar();
        UploadedCarsHelper uploadedCarsHelper = new UploadedCarsHelper(requireContext());
        Car car = uploadedCarsHelper.getCarById(carBookRequest.getCar_id());

        if (car != null) {
            BookCarLoader bookCarLoader = new BookCarLoader(requireContext(),bookingRequest,
                    ActionType.ACCEPT_BOOK);
            bookCarLoader.forceLoad();
            bookCarLoader.registerListener(8, new Loader.OnLoadCompleteListener<String>() {
                @Override
                public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                    hideProgressBar();
                    if (data != null) {
                        if (activeHire != null){
                        acceptHire(activeHire);
                        }else {
                            dismiss();
                            Toast.makeText(requireContext(), "Request not available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private String formatTime1(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    private void acceptHire(Hire hire) {
        hire.setOwner(getCurrentAccountUserName());
        hire.setOwner_contact(SimCardManager.getPhoneNumber(requireContext()));
        hire.setStatus("verified");
        reference.child(hire.getId()).setValue(hire);
        makeCarUnavailable();
        Toast.makeText(requireContext(), "Hew hire created successful", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void declineBookingRequest(CarBookRequest carBookRequest) {
        UploadedCarsHelper uploadedCarsHelper = new UploadedCarsHelper(requireContext());
        Car car = uploadedCarsHelper.getCarById(carBookRequest.getCar_id());
        if (car != null) {
            bookingRequest.setDescription("Decline");
            BookCarLoader bookCarLoader = new BookCarLoader(requireContext(),bookingRequest,
                    ActionType.DECLINE);
            bookCarLoader.forceLoad();
            bookCarLoader.registerListener(8, new Loader.OnLoadCompleteListener<String>() {
                @Override
                public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                    if (data != null) {
                        if (activeHire != null){
                            declineHire(activeHire);
                        }else {
                            dismiss();
                            Toast.makeText(requireContext(), "Request not available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void declineHire(Hire hire) {
        hire.setOwner(getCurrentAccountUserName());
        hire.setOwner_contact(SimCardManager.getPhoneNumber(requireContext()));
        hire.setStatus("declined");
        reference.child(hire.getId()).setValue(hire);
        makeCarUnavailable();
        Toast.makeText(requireContext(), "Hire decline successful", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void loadHire(CarBookRequest bookedCar) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Hire hire;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    hire = snapshot.getValue(Hire.class);
                    if (hire != null && hire.getOwner_id().equals(getCurrentAccountId()) &&
                            hire.getCarId().equals(bookedCar.getCar_id()) &&
                    hire.getStatus().equals("initialised")) {
                       activeHire = hire;
                        bookingDialogLtBinding.userPhone.setText(MessageFormat.format("Clients number: {0}",
                                hire.getClient_contact()));
                        if (hire.getStart_date() != null) {
                            bookingDialogLtBinding.date.setText(MessageFormat.format("{0} - ",
                                    formatTime(Long.parseLong(hire.getStart_date()))));
                            if (hire.getEnd_date() != null) {
                                bookingDialogLtBinding.date.setText(MessageFormat.format("{0} - {1}",
                                        formatTime(Long.parseLong(hire.getStart_date())),
                                        formatTime(Long.parseLong(hire.getEnd_date()))));
                            }
                            bookingRequest = new NewBookingRequest(
                                    hire.getClient_id(), hire.getCarId(), hire.getOwner_id(), "Accept",
                                    formatTime1(Long.parseLong(hire.getStart_date())),
                                    formatTime1(Long.parseLong(hire.getEnd_date())));
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });

    }
    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void makeCarUnavailable() {
    }

    @Override
    public void onItemClick(Vehicle item) {

    }
    public String getCurrentAccountId() {
        Context context = getContext();
        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("AccountPrefs", Context.MODE_PRIVATE);
            return sharedPreferences.getString("currentUserId", null);
        }
        return null;
    }
    public String getCurrentAccountUserName() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserName", null);
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
    private void showProgressBar() {
        bookingDialogLtBinding.progressBar.setVisibility(View.VISIBLE);
        bookingDialogLtBinding.buttonsLt.setVisibility(View.GONE);

    }

    private void hideProgressBar() {
        bookingDialogLtBinding.progressBar.setVisibility(View.GONE);
        bookingDialogLtBinding.buttonsLt.setVisibility(View.VISIBLE);
    }
}
