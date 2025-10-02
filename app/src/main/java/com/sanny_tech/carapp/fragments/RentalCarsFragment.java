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
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.UploadedCarAdapter;
import com.sanny_tech.carapp.databasehelpers.UploadedCarsHelper;
import com.sanny_tech.carapp.databinding.FragmentRentalBinding;
import com.sanny_tech.carapp.databinding.FragmentRentalCarsBinding;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.hire_utils.Hire;

import java.util.ArrayList;
import java.util.List;


public class RentalCarsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private  List<Car> availableCars;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentRentalCarsBinding binding;
    private List<Car> uploadedCars;
    private UploadedCarAdapter uploadedCarAdapter;
    private DatabaseReference reference;

    public RentalCarsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            availableCars = getArguments().getParcelableArrayList("cars");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_rental_cars, container, false);
        Toast.makeText(requireContext(), "Shown2", Toast.LENGTH_SHORT).show();
        reference = FirebaseDatabase.getInstance().getReference("hires");
        if (availableCars != null) {
            uploadedCarAdapter = new UploadedCarAdapter(requireContext(), availableCars);
            binding.recyclerView.setAdapter(uploadedCarAdapter);
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

            loadHiredStatus(availableCars);
        }

        return binding.getRoot();
    }
    private void loadHiredStatus(List<Car> carList){
        if (carList != null) {
            List<String> carIds = new ArrayList<>();
            for (Car car : carList) {
                carIds.add(car.getCar_id());
            }
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        List<Hire> hires = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Hire hire = snapshot.getValue(Hire.class);
                            if (hire != null && hire.getOwner_id().equals(getCurrentAccountId())
                            && !hire.getStatus().equals("complete") && !hire.getStatus().equals("declined")) {
                                // Notify listener about the updated location
                                hires.add(hire);
                            }
                        }
                        List<String> hiredIds = new ArrayList<>();
                        for (Hire hire: hires){
                            if (carIds.contains(hire.getCarId())){
                                hiredIds.add(hire.getCarId());
                            }
                        }
                        uploadedCarAdapter.setItems(hiredIds);
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
}