package com.sanny_tech.carapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.FunSpacesAdapter;
import com.sanny_tech.carapp.databinding.ActivityFunSpacesBinding;
import com.sanny_tech.carapp.entities.FunSpace;
import com.sanny_tech.carapp.fun_utils.SpaceDest;
import com.sanny_tech.carapp.utils.AdminManager;
import com.sanny_tech.carapp.viewPagers.FunSpaceViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class FunSpacesActivity extends AppCompatActivity implements AdminManager.AdminStatusCallback {
    private ActivityFunSpacesBinding funSpacesBinding;
    private FunSpacesAdapter funSpacesAdapter;
    private List<FunSpace> funSpaces;
    private DatabaseReference reference;
    private FirebaseDatabase database;
    private FunSpaceViewPagerAdapter pagerAdapter;
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        funSpacesBinding = DataBindingUtil.setContentView(this,R.layout.activity_fun_spaces);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        category = getIntent().getStringExtra("category");
        funSpacesBinding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("destinations");
        AdminManager manager = new AdminManager();
        manager.getAdminAccess(getCurrentAccountId(),this);

        getFunSpaces();

        funSpacesBinding.roundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FunSpacesActivity.this, CreateFunSpaceActivity.class);
                startActivity(intent);
            }
        });
        funSpacesBinding.retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFunSpaces();
            }
        });
    }

    public void getFunSpaces() {
        // This method retrieves all available taxis nearby within a certain distance

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<SpaceDest> funSpaceList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    SpaceDest spaceDest = snapshot.getValue(SpaceDest.class);
                    if (spaceDest != null &&
                            spaceDest.getCategory().equals(category)) {
                        funSpaceList.add(spaceDest);
                    }
                }
                if (!funSpaceList.isEmpty()) {
                    hideErrorLayout();
                    hideProgressBar();
                        pagerAdapter = new FunSpaceViewPagerAdapter(FunSpacesActivity.this,
                                funSpaceList,category);
                        funSpacesBinding.viewPager.setAdapter(pagerAdapter);

                        // Link ViewPager2 with TabLayout
                        new TabLayoutMediator(funSpacesBinding.tabLayout, funSpacesBinding.viewPager, (tab, position) -> {
                            // Set the title of the tab
                            tab.setText(pagerAdapter.getTitle(position));

                            // Inflate the custom tab layout
                            View tabView = LayoutInflater.from(
                                    funSpacesBinding.tabLayout.getContext()).inflate(
                                    R.layout.tab_text, null);
                            TextView tabText = tabView.findViewById(R.id.tab_text);
                            ImageView checkIcon = tabView.findViewById(R.id.check_icon);
                            tabText.setText(tab.getText());

                            // Set the custom view to the tab
                            tab.setCustomView(tabView);

                            // Show the check icon on the selected tab
                            if (position == funSpacesBinding.viewPager.getCurrentItem()) {
                                checkIcon.setVisibility(View.VISIBLE);
                            } else {
                                checkIcon.setVisibility(View.GONE);
                            }

                        }).attach();

                        // Add a TabSelectedListener to show/hide check icon
                        funSpacesBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                            @Override
                            public void onTabSelected(TabLayout.Tab tab) {
                                View tabView = tab.getCustomView();
                                if (tabView != null) {
                                    ImageView checkIcon = tabView.findViewById(R.id.check_icon);
                                    checkIcon.setVisibility(View.VISIBLE);
                                }

                                // Make sure the correct fragment is shown
                                int position = tab.getPosition();
                                funSpacesBinding.viewPager.setCurrentItem(position, false);
                            }

                            @Override
                            public void onTabUnselected(TabLayout.Tab tab) {
                                View tabView = tab.getCustomView();
                                if (tabView != null) {
                                    ImageView checkIcon = tabView.findViewById(R.id.check_icon);
                                    checkIcon.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onTabReselected(TabLayout.Tab tab) {
                                // Do nothing
                            }
                        });

                        // Set the initial tab
                        funSpacesBinding.tabLayout.selectTab(funSpacesBinding.tabLayout.getTabAt(0));

                    }else {
                    hideProgressBar();
                    showErrorLayout();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
    }

    private void showProgressBar() {
        funSpacesBinding.progressLt.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        funSpacesBinding.progressLt.setVisibility(View.GONE);
    }
    private void showErrorLayout() {
        funSpacesBinding.errorLayout.setVisibility(View.VISIBLE);
    }

    private void hideErrorLayout() {
        funSpacesBinding.errorLayout.setVisibility(View.GONE);
    }
    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs",
                MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }

    @Override
    public void onAdminStatusChecked(boolean isAdmin) {
        if (isAdmin){
            funSpacesBinding.roundButton.setVisibility(View.VISIBLE);
        }
    }
}