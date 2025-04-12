package com.sanny_tech.carapp.asynctasks;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sanny_tech.carapp.activities.MainActivity;
import com.sanny_tech.carapp.entities.UserDTO;
import com.sanny_tech.carapp.entities.UserLoginResponse;
import com.sanny_tech.carapp.enums.ActionType;
import com.sanny_tech.carapp.enums.LoginActions;
import com.sanny_tech.carapp.services.UserApiService;
import com.sanny_tech.carapp.utils.IpAddressManager;
import com.sanny_tech.carapp.utils.SimCardManager;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
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

public class UserLoader extends AsyncTaskLoader<String> {
    private static final String TAG = UserLoader.class.getSimpleName();
    private String baseUrl;
    private String email;
    private String password;
    private String token;
    private ActionType actionType;
    private String name;
    private UserDTO userRequest;

    public UserLoader(@NonNull Context context, String car_id, String password, String token, ActionType actionType, String name, UserDTO userRequest) {
        super(context);
        this.baseUrl = IpAddressManager.getIpAddress(context);
        this.email = car_id;
        this.password = password;
        this.token = token;
        this.actionType = actionType;
        this.name = name;
        this.userRequest = userRequest;
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
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

// Install the all-trusting trust manager
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl + "/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            UserApiService service = retrofit.create(UserApiService.class);
            Log.e(TAG, "UserId " + getCurrentAccountId());
            Log.e(TAG, "CarId " + email);

            if (actionType == ActionType.BOOK) {
                UserDTO userRequest = new UserDTO(email, password, name, SimCardManager.getPhoneNumber(getContext()),
                        token);
                Call<Void> call = service.createUser(userRequest);
                Response<Void> response = call.execute();
                if (response.isSuccessful()) {
                    // Handle the successful response for booking
                    return "success";
                } else {
                    Log.e(TAG, "Error: " + response.code() + " - " + response.message());
                    // Handle the error response for booking
                }
            } else if (actionType == ActionType.DELETE) {
                // Perform delete action here
                UserDTO userRequest = new UserDTO(email, password);
                Call<Void> call = service.deleteUser(userRequest);
                Response<Void> response = call.execute();
                if (response.isSuccessful()) {
                    // Handle the successful response for booking
                    return "User deleted successful";
                } else {
                    Log.e(TAG, "Error: " + response.code() + " - " + response.message());
                    // Handle the error response for booking
                }
            } else if (actionType == ActionType.UPDATE) {
                // Perform update action here
                // ...
            }else if (actionType == ActionType.GET_USER) {
                UserApiService apiService = retrofit.create(UserApiService.class);
                Call<UserLoginResponse> call = apiService.signInProfileByEmail(userRequest);
                Response<UserLoginResponse> response = call.execute();
                if (response.isSuccessful()) {
                    // Handle the successful response for booking
                    return "User exists";
                } else {
                    Log.e(TAG, "Error: " + response.code() + " - " + response.message());
                    // Handle the error response for booking
                }
            }
        } catch (IOException | KeyManagementException | NoSuchAlgorithmException e) {
            Log.e(TAG, "Error making API call: " + e.getMessage());
            return null;
        }
        return null;
    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
}
