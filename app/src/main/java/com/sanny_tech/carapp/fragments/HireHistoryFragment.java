package com.sanny_tech.carapp.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.google.firebase.database.ValueEventListener;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.AdminHireAdapter;
import com.sanny_tech.carapp.adapters.MiniPreviewImageAdapter;
import com.sanny_tech.carapp.databasehelpers.UploadedCarsHelper;
import com.sanny_tech.carapp.databinding.FragmentHireHistoryBinding;
import com.sanny_tech.carapp.databinding.FragmentRentalBinding;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.hire_utils.Hire;
import com.sanny_tech.carapp.viewPagers.RentalsViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HireHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HireHistoryFragment extends Fragment implements AdminHireAdapter.OnTaskDoneListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentHireHistoryBinding binding;
    private RentalsViewPagerAdapter pagerAdapter;
    private List<Car> uploadedCars;
    private AdminHireAdapter adminHireAdapter;

    public HireHistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HireHistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HireHistoryFragment newInstance(String param1, String param2) {
        HireHistoryFragment fragment = new HireHistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_hire_history,
                container, false);
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
        return binding.getRoot();
    }
    private void setUpViewPager(List<Car> carList) {
        if (carList != null) {
            List<String> carIds = new ArrayList<>();
            for (Car car : carList) {
                carIds.add(car.getCar_id());
            }
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("hires");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        List<Hire> hires = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Hire hire = snapshot.getValue(Hire.class);
                            if (hire != null && hire.getOwner_id().equals(getCurrentAccountId()) &&
                            !hire.getStatus().equals("complete") &&
                                    !hire.getStatus().equals("declined")) {
                                hires.add(hire);
                            }
                        }
                        updateImageRecycler(carList,hires);
                        List<String> hiredIds = new ArrayList<>();
                        for (Hire hire : hires) {
                            if (carIds.contains(hire.getCarId())) {
                                hiredIds.add(hire.getCarId());
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors if any
                }
            });
        }
    }
    private void updateImageRecycler(List<Car> carList,List<Hire> hires) {
        adminHireAdapter = new AdminHireAdapter(requireContext(),carList,hires);
        adminHireAdapter.setOnTaskDoneListener(this);
        binding.recyclerView.setAdapter(adminHireAdapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }
    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }

    @Override
    public void onTaskDone(String item) {
        Toast.makeText(requireContext(), "Refreshing...", Toast.LENGTH_SHORT).show();
        setUpViewPager(uploadedCars);
    }
}