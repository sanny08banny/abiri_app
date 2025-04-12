package com.sanny_tech.carapp.viewPagers;

import android.support.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.sanny_tech.carapp.fragments.TripListFragment;
import com.sanny_tech.carapp.taxi_utils.Trip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TripsPagerAdapter extends FragmentStateAdapter {

    private final List<String> days;
    private final Map<String, List<Trip>> tripsByDay;

    public TripsPagerAdapter(FragmentActivity fragmentActivity, Map<String, List<Trip>> tripsByDay) {
        super(fragmentActivity);
        this.tripsByDay = tripsByDay;
        this.days = new ArrayList<>(tripsByDay.keySet());
        Collections.sort(days); // Sort days chronologically if necessary
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String day = days.get(position);
        List<Trip> trips = tripsByDay.get(day);
        return TripListFragment.newInstance(trips);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }
    public String getTitle(int position) {
        return days.get(position);
    }
}
