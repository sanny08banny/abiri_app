package com.sanny_tech.carapp.viewPagers;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.sanny_tech.carapp.entities.FunSpace;
import com.sanny_tech.carapp.fragments.BookedFragment;
import com.sanny_tech.carapp.fragments.FavouritesFragment;
import com.sanny_tech.carapp.fragments.FunEventsFragment;
import com.sanny_tech.carapp.fragments.HireHistoryFragment;
import com.sanny_tech.carapp.fragments.RentalCarsFragment;
import com.sanny_tech.carapp.fragments.SpaceFragment;
import com.sanny_tech.carapp.fragments.TripsFragment;
import com.sanny_tech.carapp.fun_utils.SpaceDest;

import java.util.ArrayList;
import java.util.List;

public class FunSpaceViewPagerAdapter extends FragmentStateAdapter {
    private List<SpaceDest> funSpaces;
    private String category;

    public FunSpaceViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<SpaceDest> spaceList, String category) {
        super(fragmentActivity);
        this.funSpaces = spaceList;
        this.category = category;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        Bundle bundle = new Bundle();

        switch (position) {
            case 0:
                fragment = new SpaceFragment();
                bundle.putParcelableArrayList("spaces", new ArrayList<>(funSpaces));
                fragment.setArguments(bundle);
                break;
            case 1:
                fragment = new FunEventsFragment(category);
                break;
            default:
                fragment = new SpaceFragment();
                bundle.putParcelableArrayList("spaces", new ArrayList<>(funSpaces));
                fragment.setArguments(bundle);
                break;
        }
        return fragment;
    }



    @Override
    public int getItemCount() {
        return 2;
    }

    public String getTitle(int position) {
        switch (position) {
            case 0:
                return "Fun Spaces";
            case 1:
                return "Fun Events";
            default:
                return "Fun Spaces";
        }
    }
}

