package com.sanny_tech.carapp.viewPagers;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.fragments.HireHistoryFragment;
import com.sanny_tech.carapp.fragments.HiredCompleteFragment;
import com.sanny_tech.carapp.fragments.RentalCarsFragment;

import java.util.ArrayList;
import java.util.List;

public class RentalsViewPagerAdapter extends FragmentStateAdapter {
    private List<Car> allCars;
    private List<Car> hiredCars;
    private List<Car> availableCars;

    public RentalsViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<Car> allCars,
                                   List<String> hiredCarIds) {
        super(fragmentActivity);
        this.allCars = allCars;
        this.hiredCars = new ArrayList<>();
        this.availableCars = new ArrayList<>();
        for (Car car : allCars) {
            if (hiredCarIds.contains(car.getCar_id())) {
                hiredCars.add(car);
            } else {
                availableCars.add(car);
            }
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        Bundle bundle = new Bundle();

        switch (position) {
            case 0:
                fragment = new RentalCarsFragment();
                bundle.putParcelableArrayList("cars", new ArrayList<>(allCars));
                fragment.setArguments(bundle);
                break;
            case 1:
                fragment = new HireHistoryFragment();
                break;
            case 2:
                fragment = new RentalCarsFragment();
                bundle.putParcelableArrayList("cars", new ArrayList<>(availableCars));
                fragment.setArguments(bundle);
                break;
            case 3:
                fragment = new HiredCompleteFragment();
                break;
            default:
                fragment = new RentalCarsFragment();
                bundle.putParcelableArrayList("cars", new ArrayList<>(allCars));
                fragment.setArguments(bundle);
                break;
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    public String getTitle(int position) {
        switch (position) {
            case 0:
                return "Allcars(" + allCars.size() + ")";
            case 1:
                return "Pending";
            case 2:
                return "Availablecars(" + availableCars.size() + ")";
            case 3:
                return "Hiredcars";
            default:
                return "Allcars(" + allCars.size() + ")";
        }
    }
}

