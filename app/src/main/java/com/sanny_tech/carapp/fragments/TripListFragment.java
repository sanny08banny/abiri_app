package com.sanny_tech.carapp.fragments;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.TripAdapter;
import com.sanny_tech.carapp.databinding.FragmentTripListBinding;
import com.sanny_tech.carapp.taxi_utils.Trip;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TripListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TripListFragment extends Fragment implements TripAdapter.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentTripListBinding driverMainBinding;
    private TripAdapter tripAdapter;
    private List<Trip> trips;
    private static final String ARG_TRIPS = "arg_trips";

    public TripListFragment() {
        // Required empty public constructor
    }

    public static TripListFragment newInstance(List<Trip> trips) {
        TripListFragment fragment = new TripListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TRIPS, new ArrayList<>(trips)); // Ensure a copy to prevent modification
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trips = (List<Trip>) getArguments().getSerializable(ARG_TRIPS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        driverMainBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_trip_list, container, false);

        tripAdapter = new TripAdapter(trips,requireContext());
        tripAdapter.setOnItemClickListener(this);
        driverMainBinding.recyclerView.setAdapter(tripAdapter);
        driverMainBinding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        return driverMainBinding.getRoot();
    }
    @Override
    public void onItemClick(Trip item) {

    }
}