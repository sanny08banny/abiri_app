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

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.MiniPreviewImageAdapter;
import com.sanny_tech.carapp.asynctasks.CarUploadLoader;
import com.sanny_tech.carapp.asynctasks.CarsRetrieverLoader;
import com.sanny_tech.carapp.asynctasks.FilesUploadLoader;
import com.sanny_tech.carapp.databasehelpers.UploadedCarsHelper;
import com.sanny_tech.carapp.databinding.ActivityAddCarBinding;
import com.sanny_tech.carapp.entities.AddressItem;
import com.sanny_tech.carapp.entities.Car;
import com.google.android.material.snackbar.Snackbar;
import com.sanny_tech.carapp.enums.CarActions;
import com.sanny_tech.carapp.enums.UploadActions;
import com.sanny_tech.carapp.guides.CarDetails2Activity;
import com.sanny_tech.carapp.taxi_utils.ClientRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AddCarActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<String>, MiniPreviewImageAdapter.OnItemClickListener,
        MiniPreviewImageAdapter.OnCancelClickListener {

    private static final int IMAGE_REQUEST = 8;
    private static final int SELECT_LOCATION_REQUEST_CODE = 9;
    private ActivityAddCarBinding addCarBinding;
    private Car newCar;
    private String hourlyPrice, hourlyDownPayment, dailyPrice, dailyDownPayment,
            carId, model, description;
    private ArrayList<String> carImages;
    private ArrayList<String> selectedImagePaths = new ArrayList<>();
    private List<File> selectedFiles = new ArrayList<>();
    private MiniPreviewImageAdapter previewImageAdapter;
    private List<String> selectedLocations;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;
    private int page_count = 0;
    private AddressItem addressItem;

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
                            selectedFiles.add(new File(getPathFromUri(uri)));
                            selectedImagePaths.add(uri.toString());
                        }
                        updateImageRecycler(selectedImages);
                        page_count++;
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
        addCarBinding.uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPhotoPicker();

            }
        });
        addCarBinding.imagePlaceholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPhotoPicker();
            }
        });
        addCarBinding.ltLocationsAvailable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSelectLocationActivity();
            }
        });

        addCarBinding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePages();
            }
        });
    }

    private void handlePages() {
        if (page_count == 0) {
            model = addCarBinding.modelEdittext.getText().toString();
            carId = addCarBinding.carIdEdittext.getText().toString();
            description = addCarBinding.descriptionEdittext.getText().toString();

            dailyPrice = convertAmount(addCarBinding.dailyPriceEdittext.getText().toString());
            dailyDownPayment = convertAmount(addCarBinding.dailyDownPaymentEdittext.getText().toString());
            if (dailyPrice.length() == 0) {
                Toast.makeText(this, "Price must be set", Toast.LENGTH_SHORT).show();
            } else {
                String location = (addressItem != null) ?
                        addressItem.getAddress() : "";
                newCar = new Car(null, model, carId, getCurrentAccountId(), location,
                        description, Double.parseDouble(dailyPrice),
                        Double.parseDouble(dailyDownPayment), new ArrayList<>());
                addCarBinding.carDetails.setVisibility(View.GONE);
                addCarBinding.uploadLt.setVisibility(View.VISIBLE);
            }
        } else if (page_count == 1) {
            if (selectedImagePaths == null) {
                showSnackbar(addCarBinding.getRoot(), "You must select at least one image");
            } else {
                uploadNationalId();
            }
        }
    }

    private void openPhotoPicker() {
        pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                .build());
    }

    private void uploadCar() {
        newCar.setCar_images(selectedImagePaths);
        LoaderManager.getInstance(this).initLoader(36, null, this);
    }

    private String convertAmount(String amount) {
        // Remove commas from the input amount
        return amount.replace(",", "");
    }

    private void openSelectLocationActivity() {
        DatabaseReference hireListener = FirebaseDatabase.getInstance().getReference("configurations");
        hireListener.addValueEventListener(new ValueEventListener() {
            // Inside onDataChange method of ValueEventListener
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    String mapKey = "";
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String key = snapshot.getKey();
                        if ("maps_key".equals(key)) {
                            mapKey = snapshot.getValue(String.class);
                        }
                    }

                    if (mapKey != null) {
                        Intent intent = new Intent(AddCarActivity.this, SelectLocationActivity.class);
                        // Set extra to indicate multiple selection mode if needed
                        intent.putExtra("isMultipleSelection", false);
                        intent.putExtra("key", mapKey);
                        intent.putExtra("activity","car_hire");
                        startActivityForResult(intent, SELECT_LOCATION_REQUEST_CODE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void updateSelectedLocationText() {
        if (selectedLocations != null && !selectedLocations.isEmpty()) {
            String firstLocation = selectedLocations.get(0);
            addCarBinding.tvLocationsAvailable.setText(firstLocation);
        } else {
            addCarBinding.tvLocationsAvailable.setText("No location selected");
        }
        if (addressItem != null) {
            addCarBinding.tvLocationsAvailable.setText(addressItem.getAddress());
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
                page_count++;
            }
        } else if (requestCode == SELECT_LOCATION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            if (data.hasExtra("selectedLocations")) {
                selectedLocations = data.getStringArrayListExtra("selectedLocations");
                updateSelectedLocationText();
            } else if (data.hasExtra("selectedAddress")) {
                addressItem = data.getParcelableExtra("selectedAddress");
                updateSelectedLocationText();
            }
        }
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        showProgressBar();
        return new CarUploadLoader(this, newCar, selectedFiles,
                CarActions.UPLOAD, null);
    }

    private void showProgressBar() {
        addCarBinding.progressLt.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        addCarBinding.progressLt.setVisibility(View.GONE);

    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        hideProgressBar();
        if (data != null && data.equals("success")) {
            addCarBinding.carDetails.setVisibility(View.GONE);
            addCarBinding.uploadLt.setVisibility(View.VISIBLE);
            page_count++;
        } else {
            showSnackbar(addCarBinding.getRoot(), "Failed to upload");
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    private void showSnackbar(View rootView, String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.blue));
        snackbar.show();
    }

    private void updateImageRecycler(List<String> selectedImagePaths) {
        glideImage(selectedImagePaths.get(0));
        previewImageAdapter = new MiniPreviewImageAdapter(selectedImagePaths, this);
        previewImageAdapter.setOnItemClickListener(this);
        previewImageAdapter.setOnCancelClickListener(this);
        addCarBinding.selectedImages.setAdapter(previewImageAdapter);
        addCarBinding.selectedImages.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));
    }
    private void uploadNationalId() {
        Toast.makeText(this, "Uploading images", Toast.LENGTH_SHORT).show();
        addCarBinding.progressLt.setVisibility(View.VISIBLE);

        CarUploadLoader filesUploadLoader = new CarUploadLoader(this, newCar,
                selectedFiles, CarActions.UPLOAD, null);
        filesUploadLoader.forceLoad();
        filesUploadLoader.registerListener(7, new Loader.OnLoadCompleteListener<String>() {
            @Override
            public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                if (data != null) {
                    addCarBinding.progressLt.setVisibility(View.GONE);
                    showSnackbar(addCarBinding.getRoot(), "Image Upload\n" +
                            "Image has been uploaded successfully. Restart app to load changes");
                    setUpCar();
                } else {
                    addCarBinding.progressLt.setVisibility(View.GONE);
                    Toast.makeText(AddCarActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setUpCar() {
        Toast.makeText(this, "Setting up...", Toast.LENGTH_SHORT).show();
        CarsRetrieverLoader retrieverLoader = new CarsRetrieverLoader(AddCarActivity.this);
        retrieverLoader.forceLoad();
        retrieverLoader.registerListener(7876, new Loader.OnLoadCompleteListener<List<Car>>() {
            @Override
            public void onLoadComplete(@NonNull Loader<List<Car>> loader, @Nullable List<Car> data) {
                if (data != null) {
                    for (Car car : data) {
                        if (car.getCar_id().equals(newCar.getCar_id())) {
                            UploadedCarsHelper uploadedCarsHelper =
                                    new UploadedCarsHelper(AddCarActivity.this);
                            uploadedCarsHelper.insertCar(car);
                            showSnackbar(addCarBinding.getRoot(), "Successfully uploaded");
                            finish();
                        }
                    }
                }
            }
        });
    }

    private void glideImage(String item) {
        Glide.with(this)
                .asBitmap()
                .load(item)
                .override(ViewGroup.LayoutParams.MATCH_PARENT, 200)
                .into(addCarBinding.imagePlaceholder);
    }

    public String getPathFromUri(Uri uri) {
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String documentId = DocumentsContract.getDocumentId(uri);
            String[] split = documentId.split(":");
            if (split.length < 2) {
                Log.e("getPathFromUri", "Invalid document ID: " + documentId);
                return null;
            }
            String type = split[0];
            String id = split[1];
            Log.d("getPathFromUri", "Document ID: " + documentId + ", Type: " + type + ", ID: " + id);

            if ("image".equals(type)) {
                Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                String selection = "_id=?";
                String[] selectionArgs = new String[]{id};
                return getDataColumn(contentUri, selection, selectionArgs);
            } else if ("raw".equals(type)) {
                // Directly use the ID as the path for `raw` type
                return id;
            } else {
                Log.e("getPathFromUri", "Unsupported document type: " + type);
                return null;
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Handle content URIs from MediaStore and other providers
            return getDataColumn(uri, null, null);
        } else {
            Log.e("getPathFromUri", "URI is not a document URI: " + uri.toString());
        }
        return null;
    }


    private String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = "_data";
        String[] projection = {column};

        try {
            cursor = getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                String path = cursor.getString(index);
                Log.d("getDataColumn", "File path: " + path);
                return path;
            } else {
                Log.e("getDataColumn", "Cursor is null or empty for URI: " + uri.toString());
            }
        } catch (Exception e) {
            Log.e("getDataColumn", "Exception occurred while querying: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
    @Override
    public void onItemClick(String item) {
        glideImage(item);
    }

    @Override
    public void onCancelClick(String item) {
        int position = selectedImagePaths.indexOf(item);
        selectedImagePaths.remove(item);
        selectedFiles.remove(position);
        previewImageAdapter.removeItem(item);
    }

}