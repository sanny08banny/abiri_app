package com.sanny_tech.carapp.asynctasks;


import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.sanny_tech.carapp.enums.CarActions;
import com.sanny_tech.carapp.services.CarApiService;
import com.sanny_tech.carapp.taxi_utils.TaxiInit;
import com.sanny_tech.carapp.utils.IpAddressManager;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FunctionsLoader extends AsyncTaskLoader<ArrayList<String>> {
    private static final String TAG = FunctionsLoader.class.getSimpleName();
    private String baseUrl;
    private CarActions actionType;
    private String userId;
    private TaxiInit init;

    public FunctionsLoader(@NonNull Context context, CarActions actionType, String userId, TaxiInit init) {
        super(context);
        this.actionType = actionType;
        this.baseUrl = IpAddressManager.getIpAddress(context);
        this.userId = userId;
        this.init = init;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Nullable
    @Override
    public ArrayList<String> loadInBackground() {
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl + "/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            CarApiService service = retrofit.create(CarApiService.class);

           if (actionType == CarActions.CAR_IMAGES)  {
                Call<ArrayList<String>> call = service.fetchCarImages(getCurrentAccountId());
                Response<ArrayList<String>> response = call.execute();
                if (response.isSuccessful()) {
                    // Handle the successful response for booking
                    return response.body();
                } else {
                    Log.e(TAG, "Error: " + response.code() + " - " + response.message());
                    // Handle the error response for booking
                    return null;
                }
            }else if (actionType == CarActions.FETCH_PENDING_DOCS) {
               Call<ArrayList<String>> call = service.fetchDocs(userId,"Pending");
               Response<ArrayList<String>> response = call.execute();
               if (response.isSuccessful()) {
                   return response.body();
               } else {
                   Log.e(TAG, "Error: " + response.code() + " - " +
                           response.message());
                   return null;
               }

           }else if (actionType == CarActions.FETCH_VERIFIED_DOCS) {
               Call<ArrayList<String>> call = service.fetchDocs(userId,"Verified");
               Response<ArrayList<String>> response = call.execute();
               if (response.isSuccessful()) {
                   return response.body();
               } else {
                   Log.e(TAG, "Error: " + response.code() + " - " +
                           response.message());
                   return null;
               }

           }else if (actionType == CarActions.FETCH_UNVERIFIED_DOCS) {
               Call<ArrayList<String>> call = service.fetchDocs(userId,"Unverified");
               Response<ArrayList<String>> response = call.execute();
               if (response.isSuccessful()) {
                   return response.body();
               } else {
                   Log.e(TAG, "Error: " + response.code() + " - " +
                           response.message());
                   return null;
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
