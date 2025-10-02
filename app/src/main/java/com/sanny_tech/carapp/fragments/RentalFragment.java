package com.sanny_tech.carapp.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.AddCarActivity;
import com.sanny_tech.carapp.databasehelpers.UploadedCarsHelper;
import com.sanny_tech.carapp.databinding.FragmentRentalBinding;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.hire_utils.Hire;
import com.sanny_tech.carapp.taxi_utils.Trip;
import com.sanny_tech.carapp.viewPagers.RentalsViewPagerAdapter;
import com.sanny_tech.carapp.viewPagers.TripsPagerAdapter;
import com.sanny_tech.carapp.viewmodels.SharedViewModel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RentalFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentRentalBinding binding;
    private RentalsViewPagerAdapter pagerAdapter;
    private List<Car> uploadedCars;
    private SharedViewModel sharedViewModel;

    public RentalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_rental, container,
                false);
        loadCars();
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               loadCars();
            }
        });

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        sharedViewModel.isFabClicked().observe(requireActivity(), isClicked -> {
            if (isClicked) {

                // Reset the event after handling it
                sharedViewModel.resetFabClick();
                uploadCar();
            }
        });
        return binding.getRoot();
    }

    private void loadCars() {
        uploadedCars = new ArrayList<>();
        UploadedCarsHelper uploadedCarsHelper = new UploadedCarsHelper(requireContext());
        if (uploadedCarsHelper.getAllCars() != null) {
            for (Car car : uploadedCarsHelper.getAllCars()) {
                if (car.getOwner_id().matches(getCurrentAccountId())) {
                    uploadedCars.add(car);
                }
            }
        }
        setUpViewPager(uploadedCars);

    }

    private void setUpViewPager(List<Car> carList) {
        binding.swipeRefreshLayout.setRefreshing(false);
        showProgressBar();
        if (carList != null) {
            List<String> carIds = new ArrayList<>();
            for (Car car : carList) {
                carIds.add(car.getCar_id());
            }
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("hires");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                List<Hire> hires = new ArrayList<>();
                List<String> hiredIds = new ArrayList<>();

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        hideProgressBar();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Hire hire = snapshot.getValue(Hire.class);
                            if (hire != null && hire.getOwner_id().equals(getCurrentAccountId())) {
                                hires.add(hire);
                            }
                        }
                        for (Hire hire : hires) {
                            if (carIds.contains(hire.getCarId())) {
                                hiredIds.add(hire.getCarId());
                            }
                        }
                    }else {
                        hideProgressBar();
                    }
                    if (getActivity() != null) {
                        pagerAdapter = new RentalsViewPagerAdapter(getActivity(), uploadedCars, hiredIds);
                        binding.viewPager.setAdapter(pagerAdapter);
                        Log.d("Logs", "Vpager setup");

                        // Link ViewPager2 with TabLayout
                        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
                            // Set the title of the tab
                            tab.setText(pagerAdapter.getTitle(position));

                            // Inflate the custom tab layout
                            View tabView = LayoutInflater.from(
                                    binding.tabLayout.getContext()).inflate(
                                    R.layout.tab_text, null);
                            TextView tabText = tabView.findViewById(R.id.tab_text);
                            ImageView checkIcon = tabView.findViewById(R.id.check_icon);
                            tabText.setText(tab.getText());

                            // Set the custom view to the tab
                            tab.setCustomView(tabView);

                            // Show the check icon on the selected tab
                            if (position == binding.viewPager.getCurrentItem()) {
                                checkIcon.setVisibility(View.VISIBLE);
                            } else {
                                checkIcon.setVisibility(View.GONE);
                            }

                        }).attach();

                        // Add a TabSelectedListener to show/hide check icon
                        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                            @Override
                            public void onTabSelected(TabLayout.Tab tab) {
                                View tabView = tab.getCustomView();
                                if (tabView != null) {
                                    ImageView checkIcon = tabView.findViewById(R.id.check_icon);
                                    checkIcon.setVisibility(View.VISIBLE);
                                }

                                // Make sure the correct fragment is shown
                                int position = tab.getPosition();
                                binding.viewPager.setCurrentItem(position, false);
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
                        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0));

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors if any
                }
            });
        }
    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }

    private void showProgressBar() {
        binding.progressLt.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        binding.progressLt.setVisibility(View.GONE);
    }

    private void showErrorLayout() {
        binding.errorLayout.setVisibility(View.VISIBLE);
    }

    private void hideErrorLayout() {
        binding.errorLayout.setVisibility(View.GONE);
    }

    private void uploadCar() {
        Intent intent = new Intent(requireContext(), AddCarActivity.class);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}