package com.sanny_tech.carapp.fragments;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.FunSpaceDestAdapter;
import com.sanny_tech.carapp.adapters.FunSpacesAdapter;
import com.sanny_tech.carapp.databinding.FragmentSpaceBinding;
import com.sanny_tech.carapp.entities.FunSpace;
import com.sanny_tech.carapp.fun_utils.SpaceDest;

import java.util.ArrayList;
import java.util.List;

public class SpaceFragment extends Fragment {

    private FragmentSpaceBinding binding;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private List<SpaceDest> spaces;
    private FunSpaceDestAdapter funSpacesAdapter;

    public SpaceFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            spaces = getArguments().getParcelableArrayList("spaces");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_space, container,
                false);
        funSpacesAdapter = new FunSpaceDestAdapter(requireContext(),spaces);
        binding.carsRecycler.setAdapter(funSpacesAdapter);
        binding.carsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        return binding.getRoot();
    }
}