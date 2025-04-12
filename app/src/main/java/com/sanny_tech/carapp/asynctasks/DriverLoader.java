package com.sanny_tech.carapp.asynctasks;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sanny_tech.carapp.enums.TaxiActions;
import com.sanny_tech.carapp.services.TaxiApiService;
import com.sanny_tech.carapp.taxi_utils.ClientRequest;
import com.sanny_tech.carapp.taxi_utils.DriverAvailabilityManager;
import com.sanny_tech.carapp.taxi_utils.DriverResponse;
import com.sanny_tech.carapp.taxi_utils.TaxiDetails;
import com.sanny_tech.carapp.taxi_utils.TaxiDetailsRequest;
import com.sanny_tech.carapp.taxi_utils.TaxiInit;
import com.sanny_tech.carapp.taxi_utils.TaxiRequest;
import com.sanny_tech.carapp.utils.IpAddressManager;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DriverLoader extends AsyncTaskLoader<String> {
    private static final String TAG = DriverLoader.class.getSimpleName();
    private String baseUrl;
    private ClientRequest request;
    private TaxiActions taxiActions;
    private TaxiInit taxiInit;
    private TaxiRequest taxiRequest;

    public DriverLoader(@NonNull Context context, ClientRequest request,
                        TaxiActions taxiActions, TaxiInit taxiInit) {
        super(context);
        this.baseUrl = IpAddressManager.getIpAddress(context);
        this.request = request;
        this.taxiActions = taxiActions;
        this.taxiInit = taxiInit;
    }

    public interface ProfileFetchCallback {
        void onProfileFetched(String userId);
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
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl + "/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            TaxiApiService service = retrofit.create(TaxiApiService.class);
            Log.e(TAG, "UserId " + getCurrentAccountId());

            if (taxiActions == TaxiActions.ACCEPT) {
                DriverResponse driverResponse = new DriverResponse(request.getSender_id(),
                        getCurrentAccountId());
                Call<Void> call = service.acceptRequest(driverResponse);
                Response<Void> response = call.execute();
                if (response.isSuccessful()) {

                    // Handle the successful response for booking
                    return "success";
                } else {
                    Log.e(TAG, "Error: " + response.code() + " - " + response.message());
                    // Handle the error response for booking
                }
            }else  if (taxiActions == TaxiActions.DECLINE) {
                Call<Void> call = service.declineRequest(taxiRequest);
                Response<Void> response = call.execute();
                if (response.isSuccessful()) {

                    // Handle the successful response for booking
                    return "success";
                } else {
                    Log.e(TAG, "Error: " + response.code() + " - " + response.message());
                    // Handle the error response for booking
                }
            }  else if (taxiActions == TaxiActions.TAXI_DETAILS) {
                Call<TaxiDetails> call = service.fetchTaxiDetails(new TaxiDetailsRequest(
                        taxiInit.getTaxi_id()));
                Response<TaxiDetails> response = call.execute();
                if (response.isSuccessful()) {
                    TaxiDetails taxiDetails = response.body();
                    // Handle the successful response for booking
                    return "success";
                } else {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("logs");
                    Log.e(TAG, "Error: " + response.code() + " - " + response.message());
                    reference.child(String.valueOf(System.currentTimeMillis())).setValue(response.code() + response.message());
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
}
