package com.sanny_tech.carapp.asynctasks;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sanny_tech.carapp.enums.ActionType;
import com.sanny_tech.carapp.services.TaxiApiService;
import com.sanny_tech.carapp.taxi_utils.PricingDetails;
import com.sanny_tech.carapp.taxi_utils.TaxiRequest;
import com.sanny_tech.carapp.utils.IpAddressManager;
import com.sanny_tech.carapp.utils.RetrofitClient;
import com.sanny_tech.carapp.utils.SimCardManager;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TaxiLoader extends AsyncTaskLoader<String> {
    private static final String TAG = TaxiLoader.class.getSimpleName();
    private String baseUrl;
    private Double price;
    private PricingDetails pricingDetails;
    private ActionType actionType;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private String dest_name;
    private TaxiRequest request;
    private String category;

    public TaxiLoader(@NonNull Context context, Double price, PricingDetails pricingDetails,
                      ActionType actionType, String destName, TaxiRequest request, String category) {
        super(context);
        this.price = price;
        this.pricingDetails = pricingDetails;
        this.actionType = actionType;
        this.baseUrl = IpAddressManager.getIpAddress(context);
        dest_name = destName;
        this.request = request;
        this.category = category;
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
            TaxiApiService service = retrofit.create(TaxiApiService.class);

            database = FirebaseDatabase.getInstance();
            reference = database.getReference("verified_requests");
            Log.e(TAG, "UserId " + getCurrentAccountId());

            if (actionType == ActionType.BOOK) {
                TaxiRequest taxiRequest = new TaxiRequest();
                taxiRequest.setPricing_details(pricingDetails);
                taxiRequest.setDest_name(dest_name);
                taxiRequest.setPrice(price);
                taxiRequest.setDeclined(new ArrayList<>());
                taxiRequest.setPhone_number(SimCardManager.getPhoneNumber(getContext()));
                taxiRequest.setTaxi_category(category);
                Log.d(TAG, "Sending TaxiRequest: " + pricingDetails.toString() + "\n" +
                        taxiRequest.toString());
                Call<Void> call = service.requestTaxi(taxiRequest);
                Response<Void> response = call.execute();
                if (response.isSuccessful()) {
                    reference.child(getCurrentAccountId()).setValue(taxiRequest);
                    // Handle the successful response for booking
                    return "Booking successful";
                } else {
                    reference.child(getCurrentAccountId()).setValue(response.code() + " - " + response.message());
                    // Log the error message
                    Log.e(TAG, "Error: " + response.code() + " - " + response.message() +
                            response.body() );
                }

            } else if (actionType == ActionType.DECLINE) {
                List<String> declines = request.getDeclined();
                declines.add("declined_driver_id_" + getCurrentAccountId());
                request.setDeclined(declines);
                Call<Void> call = service.requestTaxi(request);
                Response<Void> response = call.execute();
                if (response.isSuccessful()) {
                    // Handle the successful response for booking
                    return "Decline successful";
                } else {
                    reference.child(getCurrentAccountId()).setValue(response.code() + " - " + response.message());
                    // Log the error message
                    Log.e(TAG, "Error: " + response.code() + " - " + response.message() +
                            response.body() );
                    return null;
                }

            }else if (actionType == ActionType.DELETE) {
                // Perform delete action here
//                BookingRequest bookingRequest = new BookingRequest(getCurrentAccountId(), car_id, "unbook");
//                Call<Void> call = service.bookCar(bookingRequest);
//                Response<Void> response = call.execute();
//                if (response.isSuccessful()) {
//                    // Handle the successful response for booking
//                    return "Booking successful";
//                } else {
//                    Log.e(TAG, "Error: " + response.code() + " - " + response.message());
//                    // Handle the error response for booking
                reference.child(getCurrentAccountId()).removeValue();
//                }
            } else if (actionType == ActionType.UPDATE) {
                // Perform update action here
                // ...
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
