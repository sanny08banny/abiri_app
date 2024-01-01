package com.sanny_tech.carapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.SnapHelper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.PreviewImageAdapter;
import com.sanny_tech.carapp.asynctasks.CarUploadLoader;
import com.sanny_tech.carapp.databinding.ActivityAddCarBinding;
import com.sanny_tech.carapp.entities.Car;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AddCarActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Boolean> {

    private static final int IMAGE_REQUEST = 8;
    private static final int SELECT_LOCATION_REQUEST_CODE = 9;
    private ActivityAddCarBinding addCarBinding;
    private Car newCar;
    private String hourlyPrice, hourlyDownPayment, dailyPrice, dailyDownPayment,
            carId, model, description;
    private ArrayList<String> carImages;
    private ArrayList<String> selectedImagePaths;
    private List<File> selectedFiles = new ArrayList<>();
    private PreviewImageAdapter previewImageAdapter;
    private List<String> selectedLocations;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addCarBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_car);

        ArrayList<String> selectedImages = new ArrayList<>();

// Registers a photo picker activity launcher in multi-select mode.
        pickMultipleMedia =
                registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(5), uris -> {
                    // Callback is invoked after the user selects media items or closes the
                    // photo picker.
                    if (!uris.isEmpty()) {
                        Log.d("PhotoPicker", "Number of items selected: " + uris.size());

                        // Convert URIs to strings and add them to the selectedImages list
                        for (Uri uri : uris) {
                            selectedImages.add(uri.toString());
                        }
                        updateImageRecycler(selectedImages);
                        // Now, selectedImages contains the list of selected image URIs
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });
        addCarBinding.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        addCarBinding.addImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
                    openPhotoPicker();
                } else {
                    openMediaPicker();
                }
            }
        });
        addCarBinding.ltLocationsAvailable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSelectLocationActivity();
            }
        });

        addCarBinding.submitHouse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImagePaths == null) {
                    showSnackbar(addCarBinding.getRoot(), "You must select at least one image");
                } else {
                    for (String image : selectedImagePaths) {
                        File file = new File(image);
                        selectedFiles.add(file);
                    }
                    uploadCar();
                }
            }
        });
    }

    private void openPhotoPicker() {
        pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                .build());
    }

    private void uploadCar() {
        model = addCarBinding.modelEdittext.getText().toString();
        carId = addCarBinding.carIdEdittext.getText().toString();
        description = addCarBinding.descriptionEdittext.getText().toString();

        dailyPrice = addCarBinding.dailyPriceEdittext.getText().toString();
        dailyDownPayment = addCarBinding.dailyDownPaymentEdittext.getText().toString();
        newCar = new Car(selectedImagePaths, model, carId, getCurrentAccountId(), selectedLocations.get(0),
                description, Double.parseDouble(dailyPrice),
                Double.parseDouble(dailyDownPayment), "");
        LoaderManager.getInstance(this).initLoader(36, null, this);
    }

    private void openSelectLocationActivity() {
        Intent intent = new Intent(AddCarActivity.this, SelectLocationActivity.class);
        // Set extra to indicate multiple selection mode if needed
        intent.putExtra("isMultipleSelection", true);
        startActivityForResult(intent, SELECT_LOCATION_REQUEST_CODE);
    }

    private void updateSelectedLocationText() {
        if (selectedLocations != null && !selectedLocations.isEmpty()) {
            String firstLocation = selectedLocations.get(0);
            addCarBinding.tvLocationsAvailable.setText(firstLocation);
        } else {
            addCarBinding.tvLocationsAvailable.setText("No location selected");
        }
    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }

    private void openMediaPicker() {
        Intent intent = new Intent(AddCarActivity.this, MediaPickerActivity.class);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImagePaths = data.getStringArrayListExtra("selectedImagePaths");
            if (selectedImagePaths != null) {
                Toast.makeText(this, "Items retrieved: " +
                        selectedImagePaths.size(), Toast.LENGTH_SHORT).show();

                updateImageRecycler(selectedImagePaths);
            }
        } else if (requestCode == SELECT_LOCATION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            if (data.hasExtra("selectedLocations")) {
                selectedLocations = data.getStringArrayListExtra("selectedLocations");
                updateSelectedLocationText();
            }
        }
    }

    private void updateImageRecycler(ArrayList<String> selectedImagePaths) {
        previewImageAdapter = new PreviewImageAdapter(selectedImagePaths, this);
        addCarBinding.imagesRecycler.setAdapter(previewImageAdapter);
        addCarBinding.imagesRecycler.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(addCarBinding.imagesRecycler);
    }

    @NonNull
    @Override
    public Loader<Boolean> onCreateLoader(int id, @Nullable Bundle args) {
        showProgressBar();
        return new CarUploadLoader(this, newCar, selectedFiles);
    }

    private void showProgressBar() {
        addCarBinding.progressBar.setVisibility(View.VISIBLE);
        addCarBinding.submitHouse.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        addCarBinding.progressBar.setVisibility(View.GONE);
        addCarBinding.submitHouse.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Boolean> loader, Boolean data) {
        hideProgressBar();
        if (data != null && data) {
            showSnackbar(addCarBinding.getRoot(), "Successfully uploaded");
        } else {
            showSnackbar(addCarBinding.getRoot(), "Failed to upload");
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Boolean> loader) {

    }

    private void showSnackbar(View rootView, String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.blue));
        snackbar.show();
    }
}