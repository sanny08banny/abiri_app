package com.sanny_tech.carapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.loader.content.Loader;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.asynctasks.TokenIdLoader;
import com.sanny_tech.carapp.databasehelpers.DatabaseHelper;
import com.sanny_tech.carapp.databinding.ActivityMainBinding;
import com.sanny_tech.carapp.dialogs.BookingBottomSheet;
import com.sanny_tech.carapp.entities.CarBookRequest;
import com.sanny_tech.carapp.entities.Decline;
import com.sanny_tech.carapp.entities.Ride;
import com.sanny_tech.carapp.entities.TaxiLocation;
import com.sanny_tech.carapp.entities.User;
import com.sanny_tech.carapp.storage.RemoteMessageSaver;
import com.sanny_tech.carapp.taxi_utils.ClientRequest;
import com.sanny_tech.carapp.utils.FCMTokenManager;
import com.sanny_tech.carapp.utils.IpAddressManager;
import com.sanny_tech.carapp.utils.TaxiModeManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BookingBottomSheet.TaxiBookingListener {
    private ActivityMainBinding activityMainBinding;
    private FCMTokenManager fcmTokenManager;
    private ClientRequest request;
    private DatabaseReference reference, declineReference;
    private String ip;
    private FirebaseDatabase database;
    private CarBookRequest bookingRequest;
    private BookingBottomSheet bookingBottomSheet;
    private long messageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        database = FirebaseDatabase.getInstance();
        reference = FirebaseDatabase.getInstance()
                .getReference("ip_address");
        declineReference = database.getReference("declines");
        if (getIntent().getExtras() != null) {
            if (getIntent().hasExtra("ride_id")) {
                // Type A: Contains ClientRequest details
                for (String key : getIntent().getExtras().keySet()) {
                    Object value = getIntent().getExtras().get(key);
                    Log.d("NotificationData", "Key: " + key + " Value: " + value);
                    // Handle or process the received data here
                }
                request = extractClientRequestFromIntent(getIntent());
                openDriverMaps(request);
                // Handle Type A notification with ClientRequest details
            } else if (getIntent().hasExtra("booking_id")) {
                for (String key : getIntent().getExtras().keySet()) {
                    Object value = getIntent().getExtras().get(key);
                    Log.d("NotificationData", "Key: " + key + " Value: " + value);
                    // Handle or process the received data here
                }
                bookingRequest = extractBookingRequestFromIntent(getIntent());
                showBookingWindow(bookingRequest);
            } else if (getIntent().hasExtra("id")) {
                messageId = getIntent().getLongExtra("id", 0);
                Log.d("NotificationData", "MessageId: " + messageId);
            } else {
                // Type B: Another form of notification without ClientRequest details
                String notificationMessage = getIntent().getStringExtra("message");
                // Handle Type B notification without ClientRequest details
            }
        }

        // Retrieve the message extra from the intent
        if (getIntent() != null && getIntent().hasExtra("request")) {
            if (getIntent().hasExtra("id")) {
                Log.e("id", "exists");
                messageId = getIntent().getLongExtra("id", 0);
                Log.e("NotificationData", "MessageId: " + messageId);
            }
            Object object = getIntent().getParcelableExtra("request");
            if (object instanceof ClientRequest) {
                request = (ClientRequest) object;
                if (getIntent().getAction() != null) {
                    if (getIntent().getAction().equals("ACCEPT_ACTION")) {
                        request.setStatus("Accepted");
                        openDriverMaps(request);
                    } else if (getIntent().getAction().equals("DECLINE_ACTION")) {
                        request.setStatus("Cancelled");
                        Decline decline = new Decline(getCurrentAccountId(), request.getClient_id());
                        createDecline(decline);
                        showSnackbar(activityMainBinding.getRoot(),
                                "Request decline. Click the button to change preference.");
                    }
                } else {
                    Log.e("NotificationData", "Executed");
                    openDriverMaps(request);
                }
            } else if (object instanceof CarBookRequest) {
                showBookingWindow((CarBookRequest) object);
            }
        }
        fetchAndUpdateLocalData();
        boolean isLoggedIn = getIntent().getBooleanExtra("signIn", false);
//        loadFragment(new MainFragment(isLoggedIn)); // Load the default fragment
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavigationUI.setupWithNavController(activityMainBinding.bottomNavView, navController);
        // Pass isLoggedIn value when navigating to MainFragment
        Bundle bundle = new Bundle();
        bundle.putBoolean("isLoggedIn", isLoggedIn);
        navController.setGraph(R.navigation.nav_graph);
        navController.navigate(R.id.mainFragment, bundle);

        FCMTokenManager.fetchToken(new FCMTokenManager.TokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                if (token != null) {
                    Log.d("Token", token);
                    if (getCurrentAccountId() != null &&
                            !FCMTokenManager.getToken(MainActivity.this).isEmpty() &&
                    FCMTokenManager.getToken(MainActivity.this).matches(token)) {
//                    if (isLoggedIn) {
                        TokenIdLoader tokenIdLoader = new TokenIdLoader(MainActivity.this, token);
                        tokenIdLoader.forceLoad();
                        tokenIdLoader.registerListener(7, new Loader.OnLoadCompleteListener<String>() {
                            @Override
                            public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                                if (data != null) {
                                    Log.d("TokenLoader", "success tokenId update");
                                } else {
                                    Log.d("TokenLoader", "failed tokenId update");
                                }
                            }
                        });
                    }
                }
            }
        });

        activityMainBinding.bottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.home) {
                    navController.navigate(R.id.mainFragment, bundle);
                    return true;
                } else if (item.getItemId() == R.id.drive) {
                    navController.navigate(R.id.driverMainFragment, bundle);
                    return true;
                } else if (item.getItemId() == R.id.extras) {
                    navController.navigate(R.id.extrasFragment, bundle);
                    return true;
                } else if (item.getItemId() == R.id.favourites) {
                    navController.navigate(R.id.searchFragment, bundle);
                    return true;
                }
                return false;
            }
        });
        if (getCurrentAccountType() != null) {
            toggleDriverMainFragment(TaxiModeManager.getTaxiMode(this));
        } else {
            toggleDriverMainFragment(false);
        }
        updateUser();

        checkAccountExistence();
    }
    private void changeStatusBarColor(int color){
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(color);
    }
    private void checkAccountExistence() {
        if (getCurrentAccountId() != null && !getCurrentAccountId().isEmpty()) {
            activityMainBinding.registeringBtns.setVisibility(View.GONE);
        } else {
            activityMainBinding.bottomNavView.setVisibility(View.GONE);
            activityMainBinding.registeringBtns.setVisibility(View.VISIBLE);
            activityMainBinding.signIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openSignInActivity();
                }
            });
            activityMainBinding.signUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openCreateAccountActivity();
                }
            });
        }
    }

    private void openCreateAccountActivity() {
        Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
        startActivity(intent);
    }

    private void openSignInActivity() {
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        startActivity(intent);
    }

    private void showBookingWindow(CarBookRequest bookingRequest) {
        try {
            if (messageId != 0) {
                RemoteMessageSaver.readMessageById(this, messageId);
                Log.e("booking window", "meessage read");
            }
        } catch (JSONException e) {
            Log.e("Main activity", String.valueOf(e));
            throw new RuntimeException(e);
        }
        bookingBottomSheet = new BookingBottomSheet(bookingRequest);
        bookingBottomSheet.setBookingListener(this);
        bookingBottomSheet.show(getSupportFragmentManager(), bookingBottomSheet.getTag());
    }

    private void createDecline(Decline decline) {
        declineReference.child(getCurrentAccountId()).setValue(decline);
    }

    private void fetchAndUpdateLocalData() {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Ride> rides = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ip = snapshot.getValue(String.class);
                    if (ip != null) {
                        String newBaseUrl = "http://" + ip + ":4000";
                        if (!newBaseUrl.equals(IpAddressManager.getIpAddress(MainActivity.this))) {
                            IpAddressManager.setIpAddress(MainActivity.this, ip);
                        }
                    }
                }
                if (ip == null) {
                    reference.child("ip").setValue(IpAddressManager.getIpAddress(MainActivity.this));
                }
                // You can pass this list to your UI or perform further operations
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
    }

    private void openDriverMaps(ClientRequest request) {
        try {
            if (messageId != 0) {
                RemoteMessageSaver.readMessageById(this, messageId);
            }
        } catch (JSONException e) {
            Log.e("Main activity", String.valueOf(e));
            throw new RuntimeException(e);
        }
        Intent intent = new Intent(this, TaxiMapsActivity.class);
        intent.putExtra("request", request);
        startActivity(intent);
    }

    private void toggleDriverMainFragment(boolean isInTaxiMode) {
        Menu menu = activityMainBinding.bottomNavView.getMenu();
        MenuItem driverMainMenuItem = menu.findItem(R.id.drive);

        if (driverMainMenuItem != null) {
            driverMainMenuItem.setVisible(isInTaxiMode);
        }
    }

    private void updateUser() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        if (getCurrentAccountId() != null) {
            User user = databaseHelper.getUserById(getCurrentAccountId());
            if (user != null) {
                setCurrentProfile(user);
            }
        }
    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }

    public String getCurrentPassword() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserPassword", null);
    }

    public String getCurrentEmail() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserEmail", null);
    }

    public String getCurrentAccountUserName() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserName", null);
    }

    public String getCurrentAccountType() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentAccountType", null);
    }

    private void setCurrentProfile(User selectedProfile) {
        SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences("AccountPrefs", MODE_PRIVATE);
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

    private void showSnackbar(View rootView, String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.blue));
        snackbar.setAction("Change", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        snackbar.show();
    }

    private void showTaxiConfirmationDialog(ClientRequest request) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Client request");
        builder.setMessage("You have a new ");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private ClientRequest extractClientRequestFromIntent(Intent intent) {
        ClientRequest clientRequest = new ClientRequest();

        clientRequest.setRide_id(intent.getStringExtra("ride_id"));
        clientRequest.setClient_id(intent.getStringExtra("client_id"));
        clientRequest.setUser_phone(intent.getStringExtra("user_phone"));
        clientRequest.setUser_name(intent.getStringExtra("user_name"));
        clientRequest.setDest_lat(Float.parseFloat(intent.getStringExtra("dest_lat")));
        clientRequest.setDest_lon(Float.parseFloat(intent.getStringExtra("dest_lon")));
        clientRequest.setCurrent_lat(Float.parseFloat(intent.getStringExtra("current_lat")));
        clientRequest.setCurrent_lon(Float.parseFloat(intent.getStringExtra("current_lon")));

        Log.e(MainActivity.class.getSimpleName(), String.valueOf(clientRequest.getDest_lat()));
        return clientRequest;
    }

    private CarBookRequest extractBookingRequestFromIntent(Intent intent) {
        CarBookRequest bookRequest = new CarBookRequest();

        bookRequest.setBooking_id(intent.getStringExtra("booking_id"));
        bookRequest.setClient_id(intent.getStringExtra("client_id"));
        bookRequest.setCar_id(intent.getStringExtra("car_id"));
        bookRequest.setUser_phone(intent.getStringExtra("user_phone"));
        bookRequest.setUser_name(intent.getStringExtra("user_name"));
        return bookRequest;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBookingResponse(boolean isSuccess, TaxiLocation item) {

    }
}