package com.sanny_tech.carapp.activities;

import static com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE;
import static com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.entities.CarBookRequest;
import com.sanny_tech.carapp.entities.Decline;
import com.sanny_tech.carapp.guides.HireGuideActivity;
import com.sanny_tech.carapp.guides.LocationGuideActivity;
import com.sanny_tech.carapp.guides.TaxiGuideActivity;
import com.sanny_tech.carapp.storage.RemoteMessageSaver;
import com.sanny_tech.carapp.taxi_utils.ClientRequest;
import com.sanny_tech.carapp.utils.NewAppManager;
import com.sanny_tech.carapp.utils.TypewriterEffect;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_TIMEOUT = 1500;
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 45;
    private long messageSave = 0;
    private long messageId;
    private ClientRequest request;
    private static final int REQUEST_CODE_UPDATE = 123;
    private AppUpdateManager appUpdateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SplashScreen.installSplashScreen(this);
        }
        setTheme(R.style.Base_Theme_CarApp);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        EdgeToEdge.enable(this);
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());
        appUpdateManager = AppUpdateManagerFactory.create(this);
        checkForUpdate();

        FirebaseMessaging.getInstance().subscribeToTopic(getCurrentAccountId())
                .addOnCompleteListener(task -> {
                    String msg = "Subscribed to hire notifications";
                    if (!task.isSuccessful()) {
                        msg = "Subscription failed";
                    }
                    Log.d("FCM", msg);
                });


//        TypewriterEffect someNiceText = findViewById(R.id.text2);
//        someNiceText.setCharacterDelay(150); // Adjust delay as needed
//        someNiceText.animateText("Your ride, your way");
        if (getIntent() != null && getIntent().hasExtra("request")) {
            if (getIntent().hasExtra("id")) {
                Log.e("id", "exists");
                messageId = getIntent().getLongExtra("id", 0);
                Log.e("NotificationDataSecondary", "MessageIdA: " + messageId);
            }
            Object object = getIntent().getParcelableExtra("request");

        } else {
            Log.e("NotificationDataSecondary", "MessageIdA: unfound");
        }
        Log.e("NotificationDataSecondary", "MessageIdA: " + messageId);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start MainActivity
                requestNotificationPermission();
                startMainActivity();
            }
        }, SPLASH_TIMEOUT);
    }

    private void startMainActivity() {
        if (NewAppManager.getNewApp(SplashActivity.this) && getIntent() == null){
            Intent intent = new Intent(SplashActivity.this, TaxiGuideActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            finish();
        }else {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            if (getIntent() != null){
                intent.putExtras(getIntent());
                Log.d("NotificationDataSecondary", "Moved");
            }
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            finish(); // Close splash activity
        }
    }

    private void checkForUpdate() {
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                if (appUpdateInfo.isUpdateTypeAllowed(IMMEDIATE)) {
                    // Immediate update available
                    startUpdateFlow(appUpdateInfo);
                } else if (appUpdateInfo.isUpdateTypeAllowed(FLEXIBLE)) {
                    // Flexible update available
                    startFlexibleUpdateFlow(appUpdateInfo);
                }
            }else {

            }
        }).addOnFailureListener(e -> Log.e("AppUpdate", "Error checking for update", e));
    }

    private void startUpdateFlow(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    IMMEDIATE,
                    this,
                    REQUEST_CODE_UPDATE
            );
        } catch (IntentSender.SendIntentException e) {
            Log.e("AppUpdate", "Error starting update flow", e);
        }
    }

    private void startFlexibleUpdateFlow(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    FLEXIBLE,
                    this,
                    REQUEST_CODE_UPDATE
            );
        } catch (IntentSender.SendIntentException e) {
            Log.e("AppUpdate", "Error starting flexible update flow", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_UPDATE) {
            if (resultCode == RESULT_OK) {
                Log.i("AppUpdate", "Update successful");
            } else {
                Log.e("AppUpdate", "Update failed with result code: " + resultCode);
            }
        }
    }
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 (API level 33)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_POST_NOTIFICATIONS);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMainActivity();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
}