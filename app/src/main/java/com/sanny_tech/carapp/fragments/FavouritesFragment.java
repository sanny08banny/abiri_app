package com.sanny_tech.carapp.fragments;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.RecentlyViewedCarAdapter;
import com.sanny_tech.carapp.databinding.FragmentFavouritesBinding;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.utils.DataCache;
import com.sanny_tech.carapp.utils.FavouritesManager;
import com.sanny_tech.carapp.viewmodels.CarViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FavouritesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavouritesFragment extends Fragment implements RecentlyViewedCarAdapter.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RecentlyViewedCarAdapter carAdapter;
    private CarViewModel carViewModel;
    private List<Car> cars = new ArrayList<>();
    private List<String> favourites;
    private FragmentFavouritesBinding binding;

    public FavouritesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FavouritesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FavouritesFragment newInstance(String param1, String param2) {
        FavouritesFragment fragment = new FavouritesFragment();
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
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_favourites,
                container, false);
        carAdapter = new RecentlyViewedCarAdapter(requireContext(),cars);
        carAdapter.setOnItemClickListener(this);
        binding.reviewedCarsRecycler.setAdapter(carAdapter);
        binding.reviewedCarsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        carViewModel = new ViewModelProvider(this).get(CarViewModel.class);

        // Observe changes in car data
        carViewModel.getCarListLiveData().observe(getViewLifecycleOwner(), new Observer<List<Car>>() {
            @Override
            public void onChanged(List<Car> cars) {
                // Update your UI with the new car data
                // For example, update your RecyclerView or other UI components
                carAdapter.setItems(cars);
            }
        });

        loadFavs();
        return binding.getRoot();
    }
    private void loadFavs() {
        favourites = FavouritesManager.getFavourites(requireContext());
        if (!favourites.isEmpty()){
            hideErrorLayout();
            loadCars();
        }else {
            carViewModel.setCarList(cars);
            showErrorLayout();
        }
    }

    private void loadCars() {
//        LoaderManager.getInstance(this).initLoader(1, null, this);
        List<Car> favs = new ArrayList<>();
        for (String carId : favourites){
            for (Car car : DataCache.loadData(requireContext())){
                if (car.getCar_id().equals(carId)){
                    favs.add(car);
                }
            }
        }
        carViewModel.setCarList(favs);
    }
    private void showErrorLayout() {
        binding.errorLayout.setVisibility(View.VISIBLE);
    }

    private void hideErrorLayout() {
        binding.errorLayout.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(Car item) {
        loadFavs();
    }
}