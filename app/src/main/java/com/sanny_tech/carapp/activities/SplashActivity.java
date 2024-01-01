package com.sanny_tech.carapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.utils.TypewriterEffect;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_TIMEOUT = 3500; // Splash screen timeout in milliseconds
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TypewriterEffect someNiceText = findViewById(R.id.text2);
        someNiceText.setCharacterDelay(150); // Adjust delay as needed
        someNiceText.animateText("Your ride, your way");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start MainActivity
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish(); // Close splash activity
            }
        }, SPLASH_TIMEOUT);
    }
}