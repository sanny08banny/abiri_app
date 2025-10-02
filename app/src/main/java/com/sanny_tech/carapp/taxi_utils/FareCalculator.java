package com.sanny_tech.carapp.taxi_utils;

import android.content.Context;

import com.sanny_tech.carapp.services.TaxiApiService;
import com.sanny_tech.carapp.utils.IpAddressManager;
import com.sanny_tech.carapp.utils.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FareCalculator {
    private  String BASE_URL; // Replace with your actual base URL
    private TaxiApiService taxiService;
    private Context context;

    public FareCalculator(Context context) {
        this.context = context;
        BASE_URL = IpAddressManager.getIpAddress(context) + "/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        taxiService = RetrofitClient.getClient(BASE_URL).create(TaxiApiService.class);
    }

    public void calculateFare(PricingDetails pricingDetails, FareCallback callback) {
        Call<TaxiPrice> call = taxiService.getPrice(pricingDetails);
        call.enqueue(new Callback<TaxiPrice>() {
            @Override
            public void onResponse(Call<TaxiPrice> call, Response<TaxiPrice> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception("Failed to retrieve fare"));
                }
            }

            @Override
            public void onFailure(Call<TaxiPrice> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }

    public interface FareCallback {
        void onSuccess(TaxiPrice fare);
        void onError(Exception e);
    }
}