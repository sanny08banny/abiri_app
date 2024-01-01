package com.sanny_tech.carapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.MasseuseAdapter;
import com.sanny_tech.carapp.asynctasks.CarsRetrieverLoader;
import com.sanny_tech.carapp.asynctasks.ReviewLoader;
import com.sanny_tech.carapp.databinding.ActivityMassageBinding;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.enums.ReviewAction;
import com.sanny_tech.carapp.review.Review;
import com.sanny_tech.carapp.utils.DataCache;
import com.sanny_tech.carapp.viewmodels.CarViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class MassageActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<Car>>, MasseuseAdapter.OnItemClickListener {
    private ActivityMassageBinding massageBinding;
    private CarViewModel carViewModel;
    private MasseuseAdapter masseuseAdapter;
    private List<Car> cars = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        massageBinding = DataBindingUtil.setContentView(this,R.layout.activity_massage);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        massageBinding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        masseuseAdapter = new MasseuseAdapter(this, cars);
        masseuseAdapter.setOnItemClickListener(this);
        massageBinding.masseusesRecycler.setAdapter(masseuseAdapter);
        massageBinding.masseusesRecycler.setLayoutManager(new LinearLayoutManager(this));

        carViewModel = new ViewModelProvider(this).get(CarViewModel.class);

        // Observe changes in car data
        carViewModel.getCarListLiveData().observe(this, new Observer<List<Car>>() {
            @Override
            public void onChanged(List<Car> cars) {
                // Update your UI with the new car data
                // For example, update your RecyclerView or other UI components
                masseuseAdapter.setItems(cars);
            }
        });
        loadCars();
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
        massageBinding.progressLt.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        massageBinding.progressLt.setVisibility(View.GONE);
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
                masseuseAdapter.setItems(DataCache.loadData(this));
            } else {
                showErrorLayout();
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Car>> loader) {

    }

    private void showErrorLayout() {
        massageBinding.errorLayout.setVisibility(View.VISIBLE);
    }

    private void hideErrorLayout() {
        massageBinding.errorLayout.setVisibility(View.GONE);
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
                        Toast.makeText(MassageActivity.this, "Review submitted successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MassageActivity.this, "Review not submitted.", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onItemClick(Car item) {

    }
}