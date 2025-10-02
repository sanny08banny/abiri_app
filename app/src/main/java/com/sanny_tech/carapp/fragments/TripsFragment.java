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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.TripAdapter;
import com.sanny_tech.carapp.databinding.ActivityMyTripsBinding;
import com.sanny_tech.carapp.databinding.FragmentTripsBinding;
import com.sanny_tech.carapp.taxi_utils.Trip;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TripsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TripsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TripAdapter tripAdapter;
    private FragmentTripsBinding myTripsBinding;
    private FirebaseFirestore firestore;
    private List<Trip> trips = new ArrayList<>();

    public TripsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TripsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TripsFragment newInstance(String param1, String param2) {
        TripsFragment fragment = new TripsFragment();
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
        myTripsBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_trips,
                container, false);
        firestore = FirebaseFirestore.getInstance();

        tripAdapter = new TripAdapter(trips,requireContext());
        myTripsBinding.myTrips.setAdapter(tripAdapter);
        myTripsBinding.myTrips.setLayoutManager(new LinearLayoutManager(requireContext()));

        getRidesByDriverId(getCurrentAccountId());
        return myTripsBinding.getRoot();
    }
    private void getRidesByDriverId(String driverId) {
        firestore.collection("trips")
                .whereEqualTo("user_id", driverId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Trip> receivedTrips = new ArrayList<>();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                                Trip trip = documentSnapshot.toObject(Trip.class);
                                receivedTrips.add(trip);
                            }
                            tripAdapter.setItems(receivedTrips);
                            hideErrorLayout();
                        } else {
                            // No rides found for the given driver ID
                            showErrorLayout();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                    }
                });
    }
    private void showErrorLayout() {
        myTripsBinding.errorLayout.setVisibility(View.VISIBLE);
    }

    private void hideErrorLayout() {
        myTripsBinding.errorLayout.setVisibility(View.GONE);
    }
    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(
                "AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
}