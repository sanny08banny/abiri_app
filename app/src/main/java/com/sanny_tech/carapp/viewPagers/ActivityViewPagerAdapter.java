package com.sanny_tech.carapp.viewPagers;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.fragments.BookedFragment;
import com.sanny_tech.carapp.fragments.FavouritesFragment;
import com.sanny_tech.carapp.fragments.RentalCarsFragment;
import com.sanny_tech.carapp.fragments.TripsFragment;

import java.util.ArrayList;
import java.util.List;

public class ActivityViewPagerAdapter extends FragmentStateAdapter {

    public ActivityViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new TripsFragment();
            case 1:
                return new FavouritesFragment();
            case 2:
                return new BookedFragment();
            default:
                return new TripsFragment();
        }
    }


    @Override
    public int getItemCount() {
        return 3;
    }

    public String getTitle(int position) {
        switch (position) {
            case 0:
                return "Abiri rides";
            case 1:
                return "Favourites";
            case 2:
                return "Rentals";
            default:
                return "Abiri rides";
        }
    }
}

