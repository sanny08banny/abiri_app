package com.sanny_tech.carapp.asynctasks;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sanny_tech.carapp.entities.BookingRequest;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.entities.NewBookingRequest;
import com.sanny_tech.carapp.enums.ActionType;
import com.sanny_tech.carapp.hire_utils.OwnerResponse;
import com.sanny_tech.carapp.services.CarApiService;
import com.sanny_tech.carapp.utils.IpAddressManager;
import com.sanny_tech.carapp.utils.RetrofitClient;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicMarkableReference;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BookCarLoader extends AsyncTaskLoader<String> {
    private static final String TAG = BookCarLoader.class.getSimpleName();
    private String baseUrl;
    private NewBookingRequest bookingRequest;
    private ActionType actionType;
    private Car car;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    public BookCarLoader(@NonNull Context context, NewBookingRequest bookingRequest, ActionType actionType) {
        super(context);
        this.baseUrl = IpAddressManager.getIpAddress(context) + "/";
        this.bookingRequest = bookingRequest;
        this.actionType = actionType;
        this.car = car;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Nullable
    @Override
    public String loadInBackground() {
        try {
            Retrofit retrofit = RetrofitClient.getClient(baseUrl);
            database = FirebaseDatabase.getInstance();
            reference = database.getReference("logs");

            CarApiService service = retrofit.create(CarApiService.class);

            if (actionType == ActionType.BOOK) {

                Call<Void> call = service.newBookCar(bookingRequest);
                Response<Void> response = call.execute();
                if (response.isSuccessful()) {
                    Log.e(TAG, "Error: " + response.code() + " - " + response.message());
                    reference.child(getCurrentAccountId()).setValue("Success book");
                    // Handle the successful response for booking
                    return "Booking successful";
                } else {
                    reference.child(getCurrentAccountId()).setValue(response.code() + " - " + response.message());
                    // Log the error message
                    Log.e(TAG, "Error: " + response.code() + " - " + response.message());
                }

            } else if (actionType == ActionType.DELETE) {
                Call<Void> call = service.bookCar(bookingRequest);
                Response<Void> response = call.execute();
                if (response.isSuccessful()) {
                    // Handle the successful response for booking
                    return "Booking successful";
                } else {
                    Log.e(TAG, "Error: " + response.code() + " - " + response.message());
                    // Handle the error response for booking
                }
            } else if (actionType == ActionType.UPDATE) {
                // Perform update action here
                // ...
            } else  if (actionType == ActionType.ACCEPT_BOOK) {
                Call<Void> call = service.bookCar(bookingRequest);
                Response<Void> response = call.execute();
                if (response.isSuccessful()) {
                    // Handle the successful response for booking
                    Log.e(TAG, "Error: " + response.code() + " - " + response.message());
                    return "Booking successful";
                } else {
                    Log.e(TAG, "Error: " + response.code() + " - " + response.message());
                    return null;
                    // Handle the error response for booking
                }
            }else  if (actionType == ActionType.DECLINE) {
                Call<Void> call = service.bookCar(bookingRequest);
                Response<Void> response = call.execute();
                if (response.isSuccessful()) {
                    // Handle the successful response for booking
                    Log.e(TAG, "Error: " + response.code() + " - " + response.message());
                    return "Booking successful";
                } else {
                    Log.e(TAG, "Error: " + response.code() + " - " + response.message());
                    return null;
                    // Handle the error response for booking
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error: " + e.getMessage());
            // Handle the failure
        }
        return null;
    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
    public String getCurrentEmail() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserEmail", null);
    }
}
