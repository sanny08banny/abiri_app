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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.FunSpacesAdapter;
import com.sanny_tech.carapp.databinding.FragmentFunEventsBinding;
import com.sanny_tech.carapp.entities.FunSpace;

import java.util.ArrayList;
import java.util.List;

public class FunEventsFragment extends Fragment {

    private String category;
    private DatabaseReference reference;
    private FragmentFunEventsBinding binding;
    private FunSpacesAdapter funSpacesAdapter;
    private List<FunSpace> funSpaces;

    public FunEventsFragment(String category) {
        // Required empty public constructor
        this.category = category;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_fun_events,
                container, false);
        reference = FirebaseDatabase.getInstance().getReference("fun_spaces");
        funSpaces = new ArrayList<>();
        funSpacesAdapter = new FunSpacesAdapter(requireContext(),funSpaces);
        binding.carsRecycler.setAdapter(funSpacesAdapter);
        binding.carsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        getFunSpaces();
        return binding.getRoot();
    }
    public void getFunSpaces() {
        // This method retrieves all available taxis nearby within a certain distance

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<FunSpace> funSpaceList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FunSpace funSpace = snapshot.getValue(FunSpace.class);
                    if (funSpace != null && funSpace.getDestination()  != null &&
                            funSpace.getDestination().getCategory().equals(category)) {
                        funSpaceList.add(funSpace);
                    }                }
                if (!funSpaceList.isEmpty()) {
                    hideProgressBar();
                    hideErrorLayout();
                    funSpacesAdapter.setItems(funSpaceList);
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
        binding.progressLt.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        binding.progressLt.setVisibility(View.GONE);
    }
    private void showErrorLayout() {
        binding.errorLayout.setVisibility(View.VISIBLE);
    }

    private void hideErrorLayout() {binding.errorLayout.setVisibility(View.GONE);
    }
    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs",
                MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }

}