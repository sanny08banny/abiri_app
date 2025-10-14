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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.asynctasks.FunctionsLoader;
import com.sanny_tech.carapp.asynctasks.TokenIdLoader;
import com.sanny_tech.carapp.databasehelpers.DatabaseHelper;
import com.sanny_tech.carapp.databinding.ActivityMainBinding;
import com.sanny_tech.carapp.dialogs.BookingBottomSheet;
import com.sanny_tech.carapp.entities.CarBookRequest;
import com.sanny_tech.carapp.entities.Decline;
import com.sanny_tech.carapp.entities.TaxiLocation;
import com.sanny_tech.carapp.entities.User;
import com.sanny_tech.carapp.enums.CarActions;
import com.sanny_tech.carapp.storage.RemoteMessageSaver;
import com.sanny_tech.carapp.taxi_utils.ClientRequest;
import com.sanny_tech.carapp.taxi_utils.DriverAvailabilityManager;
import com.sanny_tech.carapp.taxi_utils.TaxiInit;
import com.sanny_tech.carapp.utils.FCMTokenManager;
import com.sanny_tech.carapp.utils.IpAddressManager;
import com.sanny_tech.carapp.utils.NimbusUtils;
import com.sanny_tech.carapp.utils.RequestManager;
import com.sanny_tech.carapp.utils.SimCardManager;
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
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        database = FirebaseDatabase.getInstance();
        reference = FirebaseDatabase.getInstance()
                .getReference("configurations");
        declineReference = database.getReference("declines");

        // Retrieve the message extra from the intent
        if (getIntent() != null && getIntent().hasExtra("request")) {
            if (getIntent().hasExtra("id")) {
                Log.d("id", "exists");
                messageId = getIntent().getLongExtra("id", 0);
                Log.d("NotificationDataSecondary", "MessageIdMA: " + messageId);
            }

            Object object = getIntent().getParcelableExtra("request");
            if (object instanceof ClientRequest) {
                Log.e("NotificationData", "ClientExecuted");
                request = (ClientRequest) object;

                if (getIntent().getAction() != null) {
                    if (getIntent().getAction().equals("ACCEPT_ACTION")) {
                        request.setStatus("Accepted");
                        openDriverMaps(request);
                    } else if (getIntent().getAction().equals("DECLINE_ACTION")) {
                        request.setStatus("Cancelled");
                        Decline decline = new Decline(getCurrentAccountId(), request.getSender_id());
                        createDecline(decline);
                        showSnackbar(activityMainBinding.getRoot(),
                                "Request declined. Click the button to change preference.");
                    }
                } else {
                    Log.e("NotificationData", "Executed");
                    openDriverMaps(request);
                    Log.d("NotificationData", "Opened from 3");
                }

            } else if (object instanceof CarBookRequest) {
                showBookingWindow((CarBookRequest) object);
            } else {
                Log.e("NotificationData", "UNExecuted");
            }

            // ✅ Clear the intent to prevent reprocessing
            getIntent().removeExtra("request");
            getIntent().removeExtra("id");
            setIntent(null);  // Optional: fully reset if needed

        } else {
            Log.d("NotificationDataSecondary", "MessageIdA: unfound");
        }
        fetchAndUpdateLocalData();
        Intent intent = getIntent();
        boolean isLoggedIn = false;
        if (intent != null) {
            isLoggedIn = intent.getBooleanExtra("signIn", false);
        }//        loadFragment(new MainFragment(isLoggedIn)); // Load the default fragment
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavigationUI.setupWithNavController(activityMainBinding.bottomNavView, navController);
        // Pass isLoggedIn value when navigating to MainFragment
        Bundle bundle = new Bundle();
        bundle.putBoolean("isLoggedIn", isLoggedIn);
        navController.setGraph(R.navigation.nav_graph);
        navController.navigate(R.id.mainFragment, bundle);

//        String nimbusId = NimbusUtils.getNimbusId(this);
//        if (getCurrentAccountId() != null && !getCurrentAccountId().equals("")) {
////                    if (isLoggedIn) {
////            DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference("n_token");
////            firebaseDatabase.child(getCurrentAccountUserName()).setValue(nimbusId);
//
//            TokenIdLoader tokenIdLoader = new TokenIdLoader(MainActivity.this, nimbusId);
//            tokenIdLoader.forceLoad();
//            tokenIdLoader.registerListener(7, new Loader.OnLoadCompleteListener<String>() {
//                @Override
//                public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
//                    if (data != null) {
//                        FCMTokenManager.saveToken(MainActivity.this,nimbusId);
//                        Log.d("TokenLoader", "success tokenId update");
//                    } else {
//                        Log.d("TokenLoader", "failed tokenId update");
//                    }
//                }
//            });
//        }


        FCMTokenManager.fetchToken(new FCMTokenManager.TokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                if (token == null) {
                    Log.w("FCM", "Received null token");
                    return;
                }

                Log.d("Token", "Fetched token: " + token);

                String storedToken = FCMTokenManager.getToken(MainActivity.this);
                String userId = getCurrentAccountId();
                String username = getCurrentAccountUserName();

                // --- handle missing or empty stored token ---
                if (storedToken.isEmpty()) {
                    Log.w("FCM", "No stored token found. Saving new one.");
                    FCMTokenManager.saveToken(MainActivity.this, token);
                }

                // --- validate before proceeding ---
                if (userId != null && username != null && token.equals(storedToken)) {

                    DatabaseReference firebaseDatabase =
                            FirebaseDatabase.getInstance().getReference("n_token");

                    firebaseDatabase.child(username).setValue(token)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d("Firebase", "Token saved successfully");
                                } else {
                                    Log.e("Firebase", "Token save failed", task.getException());
                                }
                            });

                    TokenIdLoader tokenIdLoader = new TokenIdLoader(MainActivity.this, token);
                    tokenIdLoader.registerListener(7, (loader, data) -> {
                        if (data != null) {
                            Log.d("TokenLoader", "success tokenId update");
                        } else {
                            Log.d("TokenLoader", "failed tokenId update");
                        }
                    });
                    tokenIdLoader.forceLoad();

                } else {
                    // --- Case: user not logged in or token mismatch ---
                    if (userId == null || username == null) {
                        Log.w("FCM", "User not logged in; skipping token upload.");
                    } else {
                        Log.w("FCM", "Token mismatch. Updating stored token.");
                        FCMTokenManager.saveToken(MainActivity.this, token);
                    }
                }
            }
        });

        activityMainBinding.bottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.home) {
                    navController.popBackStack(R.id.mainFragment, false); // Clear back stack before navigating
                    navController.navigate(R.id.mainFragment, bundle);
                    return true;
                } else if (item.getItemId() == R.id.admin) {
                    Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out);
                    return true;
                } else if (item.getItemId() == R.id.extras) {
                    navController.popBackStack(R.id.extrasFragment, false); // Clear back stack before navigating
                    navController.navigate(R.id.extrasFragment, bundle);
                    return true;
                } else if (item.getItemId() == R.id.favourites) {
                    navController.popBackStack(R.id.searchFragment, false); // Clear back stack before navigating
                    navController.navigate(R.id.searchFragment, bundle);
                    return true;
                }
                return false;
            }
        });

        activityMainBinding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recreate();
                activityMainBinding.swipeRefreshLayout.setRefreshing(false);
            }
        });

        if (getCurrentAccountType() != null) {
            Log.e("Account type", getCurrentAccountType());
            if (getCurrentAccountType().equals("Admin")) {
                toggleDriverMainFragment(true);
                if (TaxiModeManager.getTaxiMode(this)) {
                    checkActiveRide();
                }
            } else {
                toggleDriverMainFragment(false);
            }
        } else {
            toggleDriverMainFragment(false);
        }
        updateUser();

        checkAccountExistence();
        fetchDriverDetails();
    }

    private void checkActiveRide() {
    }

    private void fetchTaxiDetails(TaxiInit init) {
        FunctionsLoader loader = new FunctionsLoader(this, CarActions.CAR_IMAGES,
                getCurrentAccountId(), init);
        loader.forceLoad();
        loader.registerListener(738, new Loader.OnLoadCompleteListener<ArrayList<String>>() {
            @Override
            public void onLoadComplete(@NonNull Loader<ArrayList<String>> loader, @Nullable ArrayList<String> data) {
                if (data != null) {
                    Log.e("Images", "loaded" + data.size());
                    init.setTaxi_images(data);
                    saveDto(init);
                    saveOrUpdateTaxiInit(init);
                    if (getCurrentAccountType() != null &&
                            !getCurrentAccountType().equals("Admin")) {
                        changeUserType();
                        restartApp();
                    }
                } else {
                    Log.e("Images", "failed");
                }
            }
        });
    }

    public void saveOrUpdateTaxiInit(TaxiInit taxiInit) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (taxiInit.getId() == null || taxiInit.getId().isEmpty()) {
            DocumentReference documentReference = db.collection("taxi_inits").document();
            String documentId = documentReference.getId();

            // Set the document ID to the TaxiInit object
            taxiInit.setId(documentId);

            // Save the TaxiInit object to Firestore with the specified ID
            documentReference.set(taxiInit)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "DocumentSnapshot added with ID: " + documentId);
                    })
                    .addOnFailureListener(e -> {
                        Log.w("Firestore", "Error adding document", e);
                    });
        } else {
            // Update existing document
            DocumentReference docRef = db.collection("taxi_inits")
                    .document(taxiInit.getId());

            docRef.set(taxiInit, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "DocumentSnapshot successfully updated!");
                    })
                    .addOnFailureListener(e -> {
                        Log.d("Firestore", "Error updating document", e);
                    });
        }
    }

    private void fetchDriverDetails() {
        List<TaxiInit> myInits = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("taxi_inits")
                .whereEqualTo("driver_id", getCurrentAccountId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                TaxiInit taxiInit = document.toObject(TaxiInit.class);
                                myInits.add(taxiInit);
                                // Handle each TaxiInit object
                                Log.e("Firestore", "TaxiInit: " + taxiInit.toString());
                            }
                            if (myInits.get(0) != null) {
                                fetchTaxiDetails(myInits.get(0));
                            } else {
                                deleteAvailabilityInit();
                            }
                        } else {
                            Log.e("FireStorefetch", "Not found");
                            deleteAvailabilityInit();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error getting documents.", e);
                    }
                });
    }

    private void deleteAvailabilityInit() {
        DriverAvailabilityManager availabilityManager = new
                DriverAvailabilityManager(MainActivity.this);
        if (availabilityManager.getTaxiInit() != null) {
            availabilityManager.deleteTaxiInit();
            availabilityManager.saveAvailabilityStatus(false);
        }
    }

    private void saveDto(TaxiInit init) {
        DriverAvailabilityManager availabilityManager = new DriverAvailabilityManager(
                MainActivity.this);
        availabilityManager.saveTaxiInit(init);
    }

    private void changeStatusBarColor(int color) {
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
        Intent intent = new Intent(MainActivity.this, AddPhoneNumberActivity.class);
        intent.putExtra("instruction", "sign-up");
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
        Log.e(TAG, "Starting fetchAndUpdateLocalData...");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, "onDataChange triggered. Children count: " + dataSnapshot.getChildrenCount());

                String ip = null;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    Log.e(TAG, "Found child: key=" + key + ", value=" + snapshot.getValue());

                    if ("ip".equals(key)) {
                        ip = snapshot.getValue(String.class);
                        Log.e(TAG, "IP found in database: " + ip);
                    }
                }

                if (ip != null) {
                    String newBaseUrl = "https://" + ip + "/api4000/v1";
                    String currentIp = IpAddressManager.getIpAddress(MainActivity.this);

                    Log.e(TAG, "Current saved IP: " + currentIp);
                    Log.e(TAG, "New Base URL: " + newBaseUrl);

                    if (!newBaseUrl.equals(currentIp)) {
                        IpAddressManager.setIpAddress(MainActivity.this, ip);
                        Log.i(TAG, "Updated IP address to: " + ip);
                    } else {
                        Log.d(TAG, "IP address unchanged.");
                    }
                } else {
                    String currentIp = IpAddressManager.getIpAddress(MainActivity.this);
                    reference.child("ip").setValue(currentIp);
                    Log.e(TAG, "No IP found in database. Setting current IP: " + currentIp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage(), databaseError.toException());
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
        }
        String accountId = request.getSender_id();
        DatabaseReference requestRef = FirebaseDatabase.getInstance()
                .getReference("verified_requests").child(accountId);

// Check if the request already exists
        requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(MainActivity.this, "Loading", Toast.LENGTH_SHORT).show();
                    loadApi(request);
                } else {
                    Toast.makeText(MainActivity.this, "Request is not available ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle potential errors here
            }
        });
    }

    private void loadApi(ClientRequest request) {
        DatabaseReference hireListener = FirebaseDatabase.getInstance().getReference("configurations");
        hireListener.addValueEventListener(new ValueEventListener() {
            // Inside onDataChange method of ValueEventListener
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    String mapKey = "";
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String key = snapshot.getKey();
                        if ("maps_key".equals(key)) {
                            mapKey = snapshot.getValue(String.class);
                        }
                    }

                    if (mapKey != null) {
                        Intent intent = new Intent(MainActivity.this,
                                TaxiMapsActivity.class);
                        intent.putExtra("key", mapKey);
                        intent.putExtra("request", request);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void toggleDriverMainFragment(boolean isInTaxiMode) {
        Menu menu = activityMainBinding.bottomNavView.getMenu();
        MenuItem driverMainMenuItem = menu.findItem(R.id.admin);

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
            } else {
                setCurrentProfile();
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

    private void setCurrentProfile() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("currentUserId");
        editor.remove("currentAccountType");
        editor.remove("currentUserEmail");
        editor.remove("currentUserName");
        editor.remove("currentDateJoined");
        editor.remove("currentUserPassword");
        editor.remove("currentProfileImage");
        editor.apply();

        if (!SimCardManager.getPhoneNumber(this).equals("")) {
            SimCardManager.setPhoneNumber(this, "");
        }
        TaxiModeManager.setTaxiMode(this, false);
        RequestManager requestManager = new RequestManager(this);
        if (requestManager.loadRequest() != null) {
            requestManager.clearRequest();
        }
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
        builder.setMessage("You have a new request");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBookingResponse(boolean isSuccess, TaxiLocation item) {

    }

    private void restartApp() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Runtime.getRuntime().exit(0);
    }

    private void changeUserType() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        User user = databaseHelper.getUserById(getCurrentAccountId());
        if (user != null) {
            user.setAccountType("Admin");
            databaseHelper.updateUser(user);
            SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("currentAccountType", "Admin");
            editor.apply();
            Toast.makeText(this, "User setup successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "You must have an account to modify", Toast.LENGTH_SHORT).show();
        }
    }
}