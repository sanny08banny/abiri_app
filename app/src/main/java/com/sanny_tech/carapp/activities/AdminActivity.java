package com.sanny_tech.carapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.TripAdapter;
import com.sanny_tech.carapp.databinding.ActivityAdminBinding;
import com.sanny_tech.carapp.fragments.DriverMainFragment;
import com.sanny_tech.carapp.fragments.MassageFragment;
import com.sanny_tech.carapp.fragments.RentalFragment;
import com.sanny_tech.carapp.taxi_utils.Trip;
import com.sanny_tech.carapp.viewmodels.SharedViewModel;

import java.util.Calendar;

public class AdminActivity extends AppCompatActivity {
    private ActivityAdminBinding binding;
    private SharedViewModel sharedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_admin);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        binding.addCar.setOnClickListener(view -> sharedViewModel.onFabClicked());
        binding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();
                if (itemId == R.id.drive) {
                    binding.addCar.setVisibility(View.GONE);
                    selectedFragment = new DriverMainFragment();
                } else if (itemId == R.id.rental) {
                    binding.addCar.setVisibility(View.VISIBLE);
                    selectedFragment = new RentalFragment();
                } else if (itemId == R.id.massage) {
                    binding.addCar.setVisibility(View.GONE);
                    selectedFragment = new MassageFragment();
                } else if (itemId == R.id.fun_space) {
                    binding.addCar.setVisibility(View.GONE);
                    Intent intent = new Intent(AdminActivity.this, MySpacesActivity.class);
                    startActivity(intent);
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, selectedFragment).commit();
                }
                return true;
            }
        });
        if (getCurrentAccountUserName() != null) {
            setGreetingMessage(getCurrentAccountUserName());
        }
        // Set the default fragment
        if (savedInstanceState == null) {
            binding.addCar.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new DriverMainFragment()).commit();
        }
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    private void setGreetingMessage(String username) {
        // Get the current hour of the day
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        // Determine the time of day and set the greeting message
        String greeting;
        if (hour >= 5 && hour < 12) {
            greeting = "Good Morning";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Good Afternoon";
        } else if (hour >= 17 && hour < 21) {
            greeting = "Good Evening";
        } else {
            greeting = "Good Night";
        }

        // Set the greeting message in the TextView
        binding.greetingTextView.setText(String.format("%s, %s!", greeting, username));
    }
    public String getCurrentAccountUserName() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserName", null);
    }
}