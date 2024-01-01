package com.sanny_tech.carapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.CarAdapter;
import com.sanny_tech.carapp.asynctasks.CarsRetrieverLoader;
import com.sanny_tech.carapp.asynctasks.ReviewLoader;
import com.sanny_tech.carapp.databinding.ActivityRentingBinding;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.enums.ReviewAction;
import com.sanny_tech.carapp.review.Review;
import com.sanny_tech.carapp.utils.DataCache;
import com.sanny_tech.carapp.viewmodels.CarViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class RentingActivity extends AppCompatActivity implements CarAdapter.OnItemClickListener,
LoaderManager.LoaderCallbacks<List<Car>>{

    private CarAdapter carAdapter;
    private List<Car> cars = new ArrayList<>();
    private ActivityRentingBinding rentingBinding;
    private CarViewModel carViewModel;
    private int selectedTabPosition;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rentingBinding = DataBindingUtil.setContentView(this,R.layout.activity_renting);

        rentingBinding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        carAdapter = new CarAdapter(this, cars);
        carAdapter.setOnItemClickListener(this);
        rentingBinding.carsRecycler.setAdapter(carAdapter);
        rentingBinding.carsRecycler.setLayoutManager(new LinearLayoutManager(this));

        carViewModel = new ViewModelProvider(this).get(CarViewModel.class);

        // Observe changes in car data
        carViewModel.getCarListLiveData().observe(this, new Observer<List<Car>>() {
            @Override
            public void onChanged(List<Car> cars) {
               carAdapter.setItems(cars);

            }
        });
        loadCars();
    }
    @Override
    public void onItemClick(Car item) {
        if (item != null) {
            showReviewBottomSheet(item);
        }
    }
    private void loadCars() {
        LoaderManager.getInstance(this).initLoader(1, null, this);
    }

    private void reloadCars() {
        LoaderManager.getInstance(this).restartLoader(1, null, this);
    }

    public String getCurrentUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }

    private void showProgressBar() {
        rentingBinding.progressLt.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        rentingBinding.progressLt.setVisibility(View.GONE);
    }

    @NonNull
    @Override
    public Loader<List<Car>> onCreateLoader(int id, @Nullable Bundle args) {
        showProgressBar();
        return new CarsRetrieverLoader(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Car>> loader, List<Car> data) {
        hideProgressBar();
        hideErrorLayout();

        if (data != null && data.size() != 0) {
            carViewModel.setCarList(data);
            DataCache.saveData(this, data);
        } else {
            if (DataCache.loadData(this) != null) {
                carViewModel.setCarList(DataCache.loadData(this));
            } else {
                showErrorLayout();
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Car>> loader) {

    }

    private void showErrorLayout() {
        rentingBinding.errorLayout.setVisibility(View.VISIBLE);
    }

    private void hideErrorLayout() {
        rentingBinding.errorLayout.setVisibility(View.GONE);
    }


    private void showReviewBottomSheet(Car item) {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_add_review, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Find views in the bottom sheet layout
        RatingBar ratingBar = bottomSheetView.findViewById(R.id.ratingBar);
        TextInputLayout feedbackTextInputLayout = bottomSheetView.findViewById(R.id.feedbackTextInputLayout);
        TextInputEditText feedbackEditText = bottomSheetView.findViewById(R.id.comment);
        MaterialButton submitButton = bottomSheetView.findViewById(R.id.submitButton);

        submitButton.setOnClickListener(v -> {
            // Get feedback and rating inputs
            String comment = feedbackEditText.getText().toString();
            float rating = ratingBar.getRating();
            Review review = new Review(getCurrentUserId(), item.getCar_id(), item.getOwner_id(),
                    "", comment, rating, "");

            ReviewLoader reviewLoader = new ReviewLoader(this, item, review, ReviewAction.CREATE);
            reviewLoader.forceLoad();

            reviewLoader.registerListener(7, new Loader.OnLoadCompleteListener<Object>() {
                @Override
                public void onLoadComplete(@NonNull Loader<Object> loader, @Nullable Object data) {
                    if (data != null) {
                        bottomSheetDialog.dismiss();
                        Toast.makeText(RentingActivity.this, "Review submitted successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RentingActivity.this, "Review not submitted.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            // Perform your submission logic here
            // You can send the feedback and rating to a server or store them locally
            // Then dismiss the bottom sheet
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

}