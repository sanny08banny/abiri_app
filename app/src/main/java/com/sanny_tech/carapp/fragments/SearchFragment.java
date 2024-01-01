package com.sanny_tech.carapp.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.SearchActivity;
import com.sanny_tech.carapp.adapters.RecentlyViewedCarAdapter;
import com.sanny_tech.carapp.databinding.FragmentSearchBinding;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.utils.DataCache;
import com.sanny_tech.carapp.utils.FavouritesManager;
import com.sanny_tech.carapp.viewmodels.CarViewModel;
import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment implements RecentlyViewedCarAdapter.OnItemClickListener {
    private FragmentSearchBinding searchBinding;

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

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
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
        searchBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_search, container, false);

        searchBinding.appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isExpanded = true;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                // Check if the CollapsingToolbarLayout is completely collapsed (verticalOffset == 0)
                // Check if the toolbar is fully collapsed and the icon is not yet visible
                if (verticalOffset != 0) {
                    if (!isExpanded) {
                        // Show the expansion icon when the toolbar is fully collapsed
                        searchBinding.expansionIndicator.setVisibility(View.VISIBLE);

                        // Reduce the horizontal margin of searchBarLayout
                        setMargin(searchBinding.searchBar, 8); // Adjust the margin value as needed
                        isExpanded = true;
                    }
                } else {
                    if (isExpanded) {
                        // Hide the expansion icon when the toolbar is not fully collapsed
                        searchBinding.expansionIndicator.setVisibility(View.GONE);

                        // Reset the horizontal margin of searchBarLayout
                        setMargin(searchBinding.searchBar, 16); // Set it back to the original margin
                        isExpanded = false;
                    }
                }
            }
        });

        carAdapter = new RecentlyViewedCarAdapter(requireContext(),cars);
        carAdapter.setOnItemClickListener(this);
        searchBinding.reviewedCarsRecycler.setAdapter(carAdapter);
        searchBinding.reviewedCarsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));

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
        searchBinding.searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearchActivity();
            }
        });

        return searchBinding.getRoot();
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

    private void openSearchActivity() {
        Intent intent = new Intent(requireContext(), SearchActivity.class);
        intent.setAction("search");
        startActivity(intent);

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

    private void setMargin(View view, int marginDp) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        int marginPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                marginDp,
                getResources().getDisplayMetrics()
        );
        layoutParams.leftMargin = marginPx;
        layoutParams.rightMargin = marginPx;
        view.setLayoutParams(layoutParams);
    }
    private void showErrorLayout() {
        searchBinding.errorLayout.setVisibility(View.VISIBLE);
    }

    private void hideErrorLayout() {
        searchBinding.errorLayout.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(Car item) {
        loadFavs();
    }
}