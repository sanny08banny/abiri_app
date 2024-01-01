package com.sanny_tech.carapp.asynctasks;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.sanny_tech.carapp.entities.UserDTO;
import com.sanny_tech.carapp.enums.ActionType;
import com.sanny_tech.carapp.services.UserApiService;
import com.sanny_tech.carapp.utils.IpAddressManager;

import java.io.IOException;

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

    public UserLoader(@NonNull Context context, String car_id, String password, String token, ActionType actionType) {
        super(context);
        this.baseUrl = IpAddressManager.getIpAddress(context);
        this.email = car_id;
        this.password = password;
        this.token = token;
        this.actionType = actionType;
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

            UserApiService service = retrofit.create(UserApiService.class);
            Log.e(TAG, "UserId " + getCurrentAccountId());
            Log.e(TAG, "CarId " + email);

            if (actionType == ActionType.BOOK) {
                UserDTO userRequest = new UserDTO(email, password,token);
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
