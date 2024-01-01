package com.sanny_tech.carapp.dialogs;

import static android.content.Context.MODE_PRIVATE;

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

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.SignInActivity;
import com.sanny_tech.carapp.adapters.TaxiAdapter;
import com.sanny_tech.carapp.asynctasks.BookCarLoader;
import com.sanny_tech.carapp.databasehelpers.UploadedCarsHelper;
import com.sanny_tech.carapp.databinding.BookingDialogLtBinding;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.entities.CarBookRequest;
import com.sanny_tech.carapp.entities.TaxiLocation;
import com.sanny_tech.carapp.enums.ActionType;
import com.sanny_tech.carapp.hire_utils.Hire;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.MessageFormat;

public class BookingBottomSheet extends BottomSheetDialogFragment implements TaxiAdapter.OnItemClickListener {
    private BookingDialogLtBinding bookingDialogLtBinding;
    private CarBookRequest carBookRequest;
    private TaxiBookingListener bookingListener;
    private DatabaseReference reference;
    private FirebaseDatabase database;

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
        return bookingDialogLtBinding.getRoot();
    }

    private void acceptBookingRequest(CarBookRequest carBookRequest) {
        UploadedCarsHelper uploadedCarsHelper = new UploadedCarsHelper(requireContext());
        Car car = uploadedCarsHelper.getCarById(carBookRequest.getCar_id());
        if (car != null) {
            BookCarLoader bookCarLoader = new BookCarLoader(requireContext(),carBookRequest.getClient_id(),
                    ActionType.ACCEPT_BOOK,car);
            bookCarLoader.forceLoad();
            bookCarLoader.registerListener(8, new Loader.OnLoadCompleteListener<String>() {
                @Override
                public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                    if (data != null){
                        Hire hire = new Hire(getCurrentAccountId(),carBookRequest.getClient_id(),
                                (float) car.getAmount(),car.getCar_id());
                        createNewHireToFirebase(hire);
                    }else {
                        Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void createNewHireToFirebase(Hire hire) {
        reference.child(getCurrentAccountId()).setValue(hire);
        makeCarUnavailable();
        Toast.makeText(requireContext(), "Hew hire created successful", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void makeCarUnavailable() {
    }

    @Override
    public void onItemClick(TaxiLocation item) {

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
}
