package com.sanny_tech.carapp.services;

import com.sanny_tech.carapp.taxi_utils.DriverResponse;
import com.sanny_tech.carapp.taxi_utils.PricingDetails;
import com.sanny_tech.carapp.taxi_utils.TaxiDetails;
import com.sanny_tech.carapp.taxi_utils.TaxiDetailsRequest;
import com.sanny_tech.carapp.taxi_utils.TaxiPrice;
import com.sanny_tech.carapp.taxi_utils.TaxiRequest;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TaxiApiService {
    @POST("taxi/request")
    Call<Void> requestTaxi(@Body TaxiRequest taxiRequest);
    @POST("taxi/accept")
    Call<Void> acceptRequest(@Body DriverResponse response);
    @POST("taxi/details")
    Call<TaxiDetails> fetchTaxiDetails(@Body TaxiDetailsRequest taxiId);
    @POST("taxi/price")
    Call<TaxiPrice> getPrice(@Body PricingDetails pricingDetails);
    @POST("taxi/decline")
    Call<Void> declineRequest(TaxiRequest taxiRequest);
}
