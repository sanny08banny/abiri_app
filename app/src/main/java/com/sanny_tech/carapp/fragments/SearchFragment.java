package com.sanny_tech.carapp.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.databinding.FragmentActivityBinding;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.viewPagers.ActivityViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentActivityBinding binding;
    private ActivityViewPagerAdapter pagerAdapter;
    private List<Car> uploadedCars;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RentalFragment.
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_activity, container,
                false);
        uploadedCars = new ArrayList<>();

        setUpViewPager();
        return binding.getRoot();
    }

    private void setUpViewPager() {
                        if (getActivity() != null) {
                            pagerAdapter = new ActivityViewPagerAdapter(getActivity());
                            binding.viewPager.setAdapter(pagerAdapter);

                            // Link ViewPager2 with TabLayout
                            new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
                                // Set the title of the tab
                                tab.setText(pagerAdapter.getTitle(position));

                                // Inflate the custom tab layout
                                View tabView = LayoutInflater.from(
                                        binding.tabLayout.getContext()).inflate(
                                        R.layout.tab_text, null);
                                TextView tabText = tabView.findViewById(R.id.tab_text);
                                ImageView checkIcon = tabView.findViewById(R.id.check_icon);
                                tabText.setText(tab.getText());

                                // Set the custom view to the tab
                                tab.setCustomView(tabView);

                                // Show the check icon on the selected tab
                                if (position == binding.viewPager.getCurrentItem()) {
                                    checkIcon.setVisibility(View.VISIBLE);
                                } else {
                                    checkIcon.setVisibility(View.GONE);
                                }

                            }).attach();

                            // Add a TabSelectedListener to show/hide check icon
                            binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                                @Override
                                public void onTabSelected(TabLayout.Tab tab) {
                                    View tabView = tab.getCustomView();
                                    if (tabView != null) {
                                        ImageView checkIcon = tabView.findViewById(R.id.check_icon);
                                        checkIcon.setVisibility(View.VISIBLE);
                                    }

                                    // Make sure the correct fragment is shown
                                    int position = tab.getPosition();
                                    binding.viewPager.setCurrentItem(position, false);
                                }

                                @Override
                                public void onTabUnselected(TabLayout.Tab tab) {
                                    View tabView = tab.getCustomView();
                                    if (tabView != null) {
                                        ImageView checkIcon = tabView.findViewById(R.id.check_icon);
                                        checkIcon.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onTabReselected(TabLayout.Tab tab) {
                                    // Do nothing
                                }
                            });

                            // Set the initial tab
                            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0));

                        }
    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
}