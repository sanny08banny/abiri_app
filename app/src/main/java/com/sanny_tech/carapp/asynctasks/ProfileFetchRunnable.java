package com.sanny_tech.carapp.asynctasks;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sanny_tech.carapp.activities.MainActivity;
import com.sanny_tech.carapp.databasehelpers.DatabaseHelper;
import com.sanny_tech.carapp.entities.AdminAccessRequest;
import com.sanny_tech.carapp.entities.User;
import com.sanny_tech.carapp.entities.UserDTO;
import com.sanny_tech.carapp.entities.UserLoginResponse;
import com.sanny_tech.carapp.enums.LoginActions;
import com.sanny_tech.carapp.services.UserApiService;
import com.sanny_tech.carapp.utils.FCMTokenManager;
import com.sanny_tech.carapp.utils.IpAddressManager;
import com.sanny_tech.carapp.utils.SimCardManager;
import com.sanny_tech.carapp.utils.TaxiModeManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileFetchRunnable implements Runnable {
    private final String email;
    private final String password;
    private final Context context;
    private DatabaseHelper dataBaseHelper;
    private ProgressBar progressBar;
    private boolean isLogin;
    private LoginActions loginActions;
    private UserLoader.ProfileFetchCallback callback;
    private OnFinishLoadListener listener;
    private String name;
    private UserLoginResponse loginResponse;

    public ProfileFetchRunnable(String email, String password, Context context,
                                ProgressBar progressBar, LoginActions loginActions,
                                UserLoader.ProfileFetchCallback callback, String name) {
        this.email = email;
        this.password = password;
        this.context = context;
        this.progressBar = progressBar;
        this.loginActions = loginActions;
        this.callback = callback;
        this.name = name;
    }
    public interface OnFinishLoadListener {
        void onResponse(String response);
    }
    public void setOnFinishLoadListener(OnFinishLoadListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        // Make API call to retrieve the profile account using the provided email
        // Replace the URL with your actual Spring application endpoint
        try {
            if (progressBar != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE); // Show the ProgressBar
                    }
                });
            }
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
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .build();
            String baseUrl = IpAddressManager.getIpAddress(context);
            String apiUrl = baseUrl + "/";
            Log.e("ProfileFetchRunnable", "Email chosen: " + apiUrl);

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(apiUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();

            UserApiService apiService = retrofit.create(UserApiService.class);
            UserDTO userRequest = new UserDTO(email, password, name,
                    SimCardManager.getPhoneNumber(context),
                    FCMTokenManager.getToken(context));

            Call<UserLoginResponse> call = null;
            if (loginActions == LoginActions.LOGIN) {
                call = apiService.signInProfileByEmail(userRequest);
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("logs");
                reference.child(String.valueOf(System.currentTimeMillis())).setValue(apiUrl);
            } else if (loginActions == LoginActions.ADMIN_ACCESS) {
                AdminAccessRequest request = new AdminAccessRequest(getCurrentAccountId(), "admin");
                call = apiService.signInProfileByEmail(userRequest);
            } else if (loginActions == LoginActions.DRIVER_ACCESS) {
                AdminAccessRequest request = new AdminAccessRequest(
                        getCurrentAccountId(), "driver");
                call = apiService.getDriverAccess(request);
            }

            if (call != null) {
                call.enqueue(new Callback<UserLoginResponse>() {

                    @Override
                    public void onResponse(@NonNull Call<UserLoginResponse> call, @NonNull Response<UserLoginResponse> response) {
                       loginResponse = response.body();
                        if (loginResponse != null) {
                            String userId = loginResponse.getUser_id();
                            if (userId != null) {
                                // Save the profile account to the SQLite database
                                showToast("Login successful");
                                saveProfileToDatabase(userId, loginResponse.getIs_driver());
                                Log.e("ProfileFetchSuccess", loginResponse.toString());

                                if (callback == null) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (progressBar != null) {
                                                progressBar.setVisibility(View.GONE);// Show the ProgressBar
                                            }
                                            Intent intent = new Intent(context, MainActivity.class);
                                            intent.putExtra("signIn", true);
                                            context.startActivity(intent);
                                            ((Activity) context).finish();
                                        }
                                    });
                                } else {
                                    if (progressBar != null) {
                                        progressBar.setVisibility(View.GONE);// Show the ProgressBar
                                    }
                                    callback.onProfileFetched(userId);
                                }
                            }
                        } else {
                            showToast("Bad credentials. Please try again");
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);// Show the ProgressBar
                            }
                            if (listener != null) {
                                listener.onResponse(response.message());
                            }
                            Log.e("login error", response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<UserLoginResponse> call, Throwable t) {
                        showToast("Oops something went wrong !!! " +
                                "Please make sure you have an account");
                        if (t != null && t.getMessage() != null) {
                            Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            } else {
                Toast.makeText(context, "Something went wrong please retry in a minute.", Toast.LENGTH_SHORT).show();
            }
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            Log.e(ProfileFetchRunnable.class.getSimpleName(), "Error making API call: " + e.getMessage());
        }
    }

    private void setCurrentProfile(User selectedProfile) {
        if (loginActions == LoginActions.FUN_ADMIN_ACCESS){
            selectedProfile.setAccountType("Admin");
        }
        SimCardManager.setPhoneNumber(context,loginResponse.getUser_phone());
        SharedPreferences sharedPreferences = context.getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("currentUserId", selectedProfile.getUserId());
        editor.putString("currentAccountType", selectedProfile.getAccountType());
        editor.putString("currentUserEmail", selectedProfile.getEmail());
        editor.putString("currentUserName", selectedProfile.getUsername());
        editor.putString("currentDateJoined", selectedProfile.getDateCreated());
        editor.putString("currentUserPassword", selectedProfile.getPassword());
        editor.putString("currentProfileImage", selectedProfile.getProfilePic());
        editor.apply();
    }

    private void saveProfileToDatabase(String profileJson, Boolean isDriver) {
        // Parse the JSON or extract necessary data and save it to the SQLite database
        // Use your preferred database library (e.g., Room, SQLiteOpenHelper, etc.)
        dataBaseHelper = new DatabaseHelper(context);
        User user = dataBaseHelper.getUserById(profileJson);
        if (user == null) {
            User newUser = new User();
            newUser.setPassword(password);
            newUser.setEmail(email);
            newUser.setUserId(profileJson);
            newUser.setUsername(loginResponse.getUser_name());
            newUser.setDateCreated(String.valueOf(new Date()));

            if (isDriver) {
                newUser.setAccountType("Admin");
            } else {
                newUser.setAccountType("User");
            }
            if (isDriver) {
                TaxiModeManager.setTaxiMode(context, true);
            }
            setCurrentProfile(newUser);
            String accountSaved = dataBaseHelper.addUser(newUser);
            if (accountSaved != null) {
                Toast.makeText(context, "New user saved here " + accountSaved, Toast.LENGTH_SHORT).show();
            }
        } else {
            user.setUserId(profileJson);
            user.setPhoneNumber(loginResponse.getUser_phone());
            user.setUsername(loginResponse.getUser_name());
            if (isDriver != null && isDriver) {
                user.setAccountType("Admin");
            } else {
                user.setAccountType("User");
            }
            TaxiModeManager.setTaxiMode(context, isDriver != null && isDriver);
            setCurrentProfile(user);
            int accountSavedLocally = dataBaseHelper.updateUser(user);
            Toast.makeText(context, "User saved here " + accountSavedLocally, Toast.LENGTH_SHORT).show();
        }

    }

    private void showToast(final String message) {
        // Show a toast message on the UI thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
}