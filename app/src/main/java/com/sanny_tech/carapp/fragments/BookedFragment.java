package com.sanny_tech.carapp.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.BookedActivity;
import com.sanny_tech.carapp.adapters.BookedCarAdapter;
import com.sanny_tech.carapp.databinding.FragmentBookedBinding;
import com.sanny_tech.carapp.hire_utils.Hire;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BookedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BookedFragment extends Fragment implements BookedCarAdapter.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentBookedBinding extrasBinding;
    private BookedCarAdapter bookedCarAdapter;
    private List<Hire> hires = new ArrayList<>();

    public BookedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BookedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BookedFragment newInstance(String param1, String param2) {
        BookedFragment fragment = new BookedFragment();
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
        extrasBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_booked,
                container, false);
        bookedCarAdapter = new BookedCarAdapter(hires, requireContext());
        bookedCarAdapter.setOnItemClickListener(this);

        extrasBinding.bookedCars.setAdapter(bookedCarAdapter);
        extrasBinding.bookedCars.setLayoutManager(new LinearLayoutManager(requireContext()));
        fetchHires();

        extrasBinding.bookedCarsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBookedCarsActivity();
            }
        });

//        if (bookedCars.size() == 0) {
//            showErrorLayoutNothing();
//        } else {
//            hideErrorLayout();
//        }
        return extrasBinding.getRoot();
    }

    private void fetchHires() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("hires");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Hire> hireList = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Hire hire = snapshot.getValue(Hire.class);
                        if (hire != null && hire.getClient_id().equals(getCurrentAccountId())) {
                            hireList.add(hire);
                        }
                    }
                    updateImageRecycler(hireList);
                }else {
                    showErrorLayout();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
    }
    private void updateImageRecycler(List<Hire> hires) {
        Log.d("Hires" , String.valueOf(hires.size()));
        bookedCarAdapter.setItems(hires);
    }
    private void showErrorLayout() {
        extrasBinding.errorLayout.setVisibility(View.VISIBLE);
    }

    private void hideErrorLayout() {
        extrasBinding.errorLayout.setVisibility(View.GONE);
    }

    private void showErrorLayoutNothing() {
        extrasBinding.errorLayout.setVisibility(View.VISIBLE);
        extrasBinding.errorText.setText("Nothing to show");
    }


    private void openBookedCarsActivity() {
        Intent intent = new Intent(requireContext(), BookedActivity.class);
        startActivity(intent);
    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }

    public String getCurrentPassword() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserPassword", null);
    }

    public String getCurrentEmail() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserEmail", null);
    }

    public String getCurrentAccountUserName() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserName", null);
    }

    public String getCurrentAccountType() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentAccountType", null);
    }

    /**
     * @param item
     */
    @Override
    public void onItemClick(Hire item) {

    }
}