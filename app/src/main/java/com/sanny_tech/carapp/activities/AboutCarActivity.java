package com.sanny_tech.carapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.databinding.DataBindingUtil;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.CommentAdapter;
import com.sanny_tech.carapp.asynctasks.CarUploadLoader;
import com.sanny_tech.carapp.asynctasks.ReviewLoader;
import com.sanny_tech.carapp.databasehelpers.UploadedCarsHelper;
import com.sanny_tech.carapp.databinding.ActivityAboutCarBinding;
import com.sanny_tech.carapp.dialogs.ProgressDialogFragment;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.enums.CarActions;
import com.sanny_tech.carapp.enums.ReviewAction;
import com.sanny_tech.carapp.hire_utils.Hire;
import com.sanny_tech.carapp.hire_utils.HireActivity;
import com.sanny_tech.carapp.hire_utils.HireManager;
import com.sanny_tech.carapp.review.CarReviewResponse;
import com.sanny_tech.carapp.viewPagers.ImagePagerAdapter;
import com.sanny_tech.carapp.utils.IpAddressManager;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AboutCarActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Object>{
    private ActivityAboutCarBinding aboutCarBinding;
    private Car car;
    private ProgressDialogFragment progressDialogFragment;
    private String duration;
    private HireManager hireManager;
    private DatabaseReference reference;
    private FirebaseDatabase database;
    private ImagePagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aboutCarBinding = DataBindingUtil.setContentView(this, R.layout.activity_about_car);
        car = getIntent().getParcelableExtra("selectedCar");
        hireManager = new HireManager(this,car.getCar_id());
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("hires");
        aboutCarBinding.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if (car != null) {

            aboutCarBinding.description.setText(car.getDescription());
            aboutCarBinding.model.setText(car.getModel());
            aboutCarBinding.location.setText(car.getLocation());
            double amount = car.getDaily_amount();
            Locale kenyanLocale = new Locale("sw", "KE");
            Currency kenyanShilling = Currency.getInstance("KES");
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(kenyanLocale);
            numberFormat.setCurrency(kenyanShilling);
            String formattedAmount = numberFormat.format(amount);

            aboutCarBinding.price.setText(formattedAmount);
        }
        ArrayList<String> images = new ArrayList<>();
        String instruction = getIntent().getStringExtra("instruction");
        if (instruction != null && instruction.equals("local")) {
            images.addAll(car.getCar_images());
            aboutCarBinding.deleteLi.setVisibility(View.VISIBLE);
            aboutCarBinding.deleteCarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        } else {
            for (String filePath : car.getCar_images()) {
                String baseUrl = IpAddressManager.getIpAddress(this);
                String endPoint = baseUrl + "/car/image/" + car.getOwner_id() + "/"
                        + car.getCar_id() + "/" + filePath;
                images.add(endPoint);
            }
        }

        adapter = new ImagePagerAdapter(this, images);
        aboutCarBinding.houseImagesViewPager.setAdapter(adapter);
        aboutCarBinding.houseImagesViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updatePageIndicator(position);
            }
        });

        // Initialize the indicator text
        updatePageIndicator(0);
        loadHire();

        aboutCarBinding.findCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(AboutCarActivity.this, car);
            }
        });
        aboutCarBinding.deleteCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
        loadRating();
    }

    private void loadRating() {
        LoaderManager.getInstance(this).initLoader(1, null, this);
    }

    private void updatePageIndicator(int position) {
        int total = adapter.getItemCount();
        aboutCarBinding.count.setText((position + 1) + "/" + total);
    }

    public void removeDots() {
        aboutCarBinding.dotsIndicator.removeAllViews();
    }

    private int calculateDuration(MaterialTimePicker fromTimePicker, MaterialTimePicker toTimePicker) {
        int fromHour = fromTimePicker.getHour();
        int fromMinute = fromTimePicker.getMinute();
        int toHour = toTimePicker.getHour();
        int toMinute = toTimePicker.getMinute();

        // Calculate the duration in minutes
        int durationInMinutes = (toHour - fromHour) * 60 + (toMinute - fromMinute);

        return durationInMinutes;
    }

    private void showDatePickerDialog(Context context, Car car) {
        // Create a MaterialDatePicker for selecting a date range
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date Range")
                .setSelection(Pair.create(System.currentTimeMillis(), System.currentTimeMillis())) // Initial selection (today)
                .build();

        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long, Long> selection) {
                long fromDateMillis = selection.first;
                long toDateMillis = selection.second;

                // Convert milliseconds to a duration string
                long durationMillis = toDateMillis - fromDateMillis;
                long days = TimeUnit.MILLISECONDS.toDays(durationMillis);
                long hours = TimeUnit.MILLISECONDS.toHours(durationMillis) - TimeUnit.DAYS.toHours(days);
                duration = String.format(Locale.US, "%d days", days);

                // Call the bookCar method with the car and duration
                bookCar(car,fromDateMillis,toDateMillis);
            }
        });


        picker.show(((AppCompatActivity) context).getSupportFragmentManager(), picker.toString());
    }

    private void bookCar(Car car, long fromDateMillis, long toDateMillis) {
        Intent intent = new Intent(this, HireActivity.class);
        intent.setAction("book car");
        intent.putExtra("car", car);
        intent.putExtra("from", fromDateMillis);
        intent.putExtra("to", toDateMillis);
        startActivity(intent);
    }

    private void showProgreeBar() {
        progressDialogFragment = new ProgressDialogFragment();
        progressDialogFragment.show(getSupportFragmentManager(), "progress_dialog");
    }

    private void hideProgreeBar() {
        progressDialogFragment.dismiss();
    }

    @NonNull
    @Override
    public Loader<Object> onCreateLoader(int id, @Nullable Bundle args) {
        return new ReviewLoader(this, car, null, ReviewAction.GET);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Object> loader, Object data) {
        if (data instanceof CarReviewResponse) {
            CarReviewResponse carReviewResponse = (CarReviewResponse) data;
            aboutCarBinding.ratingLt.setVisibility(View.VISIBLE);
            aboutCarBinding.carRatingBar.setRating((float) carReviewResponse.getAverageRating());
            aboutCarBinding.carRatingText.setText(String.valueOf(carReviewResponse.getAverageRating()));
            if (carReviewResponse.getComments() != null) {
                CommentAdapter commentAdapter = new CommentAdapter(carReviewResponse.getComments(),
                        AboutCarActivity.this);
                aboutCarBinding.commentsRecycler.setAdapter(commentAdapter);
                aboutCarBinding.commentsRecycler.setLayoutManager(new LinearLayoutManager(this));
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Object> loader) {

    }

    private void loadHire() {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Hire hire;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    hire = snapshot.getValue(Hire.class);
                    if (hire != null && hire.getClient_id().equals(getCurrentAccountId()) &&
                            hire.getCarId().equals(car.getCar_id()) &&
                                    !hire.getStatus().equals("complete")) {
                        aboutCarBinding.hiredStatus.setVisibility(View.VISIBLE);
                    }
                }
                // You can pass this list to your UI or perform further operations

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });

    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs",
                MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.scale_up, R.anim.scale_down);
    }
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account")
                .setMessage("Are you sure you want to delete this car?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Perform the deletion here
                        deleteCar();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCar() {
        CarUploadLoader carUploadLoader = new CarUploadLoader(this,car,null,
                CarActions.DELETE,null);
        carUploadLoader.forceLoad();

        carUploadLoader.registerListener(8, new Loader.OnLoadCompleteListener<String>() {
            @Override
            public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                if (data != null && data.equals("success")){
                    UploadedCarsHelper uploadedCarsHelper = new UploadedCarsHelper(AboutCarActivity.this);
                    uploadedCarsHelper.deleteCar(car.getCar_id());

                    Toast.makeText(AboutCarActivity.this, "Car deleted successful", Toast.LENGTH_SHORT).show();
                    finish();
                }else {
                    Toast.makeText(AboutCarActivity.this, "Action failed. Retry or try again later", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}