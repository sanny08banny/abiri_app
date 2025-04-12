package com.sanny_tech.carapp.guides;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.MainActivity;
import com.sanny_tech.carapp.activities.SplashActivity;
import com.sanny_tech.carapp.adapters.MiniPreviewImageAdapter;
import com.sanny_tech.carapp.asynctasks.DriverLoader;
import com.sanny_tech.carapp.asynctasks.FilesUploadLoader;
import com.sanny_tech.carapp.asynctasks.FunctionsLoader;
import com.sanny_tech.carapp.asynctasks.ProfileFetchRunnable;
import com.sanny_tech.carapp.databasehelpers.DatabaseHelper;
import com.sanny_tech.carapp.databinding.ActivityCarDetails2Binding;
import com.sanny_tech.carapp.databinding.PasswordInputBinding;
import com.sanny_tech.carapp.entities.User;
import com.sanny_tech.carapp.enums.CarActions;
import com.sanny_tech.carapp.enums.LoginActions;
import com.sanny_tech.carapp.enums.TaxiActions;
import com.sanny_tech.carapp.enums.UploadActions;
import com.sanny_tech.carapp.taxi_utils.DriverAvailabilityManager;
import com.sanny_tech.carapp.taxi_utils.TaxiInit;
import com.sanny_tech.carapp.utils.AdminManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CarDetails2Activity extends AppCompatActivity implements MiniPreviewImageAdapter.OnItemClickListener,
        MiniPreviewImageAdapter.OnCancelClickListener {

    private ActivityCarDetails2Binding binding;
    private long page_count = 0;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;
    private List<Uri> selectedFiles = new ArrayList<>();
    private List<String> selectedImagePaths = new ArrayList<>();
    private MiniPreviewImageAdapter previewImageAdapter;
    private int defaultColor;
    private List<TaxiInit> myInits = new ArrayList<>();
    private PasswordInputBinding passwordInputBinding;
    private AdminManager adminManager;
    private TaxiInit init;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_car_details2);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        init = getIntent().getParcelableExtra("init");
        pickMultipleMedia =
                registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(5), uris -> {
                    // Callback is invoked after the user selects media items or closes the
                    // photo picker.
                    if (!uris.isEmpty()) {
                        Log.d("PhotoPicker", "Number of items selected: " + uris.size());

                        // Convert URIs to strings and add them to the selectedImages list
                        for (Uri uri : uris) {
                            selectedFiles.add(uri);
                            selectedImagePaths.add(uri.toString());
                        }
                        updateImageRecycler(selectedImagePaths);
                        // Now, selectedImages contains the list of selected image URIs
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImagePaths.isEmpty()) {
                    Toast.makeText(CarDetails2Activity.this,
                            "You must select at least one image", Toast.LENGTH_SHORT).show();
                } else {
                    uploadNationalId();
                }
            }
        });
        binding.uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPhotoPicker();
            }
        });
        binding.imagePlaceholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPhotoPicker();
            }
        });
    }

    private void showSnackbar(View rootView, String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.green));
        snackbar.setTextColor(ContextCompat.getColor(this,R.color.white));
        snackbar.show();
    }

    private void uploadNationalId() {
        List<File> files = new ArrayList<>();
        for (Uri uri : selectedFiles) {
            File idFrontFile = new File(getPathFromUri(uri));
            files.add(idFrontFile);
        }
        Toast.makeText(this, "Uploading images", Toast.LENGTH_SHORT).show();
        binding.finishProgressBar.setVisibility(View.VISIBLE);

        FilesUploadLoader filesUploadLoader = new FilesUploadLoader(this, null,
                null, UploadActions.UPLOAD_IMAGES, init, files);
        filesUploadLoader.forceLoad();
        filesUploadLoader.registerListener(7, new Loader.OnLoadCompleteListener<String>() {
            @Override
            public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                if (data != null) {
                    updateUserProgress(getCurrentAccountId(), "national_id", true);
                    binding.finishProgressBar.setVisibility(View.GONE);
                    showSnackbar(binding.getRoot(), "Image Upload\n" +
                            "Image has been uploaded successfully. Restart app to load changes");
                    fetchTaxiDetails();
                } else {
                    binding.finishProgressBar.setVisibility(View.GONE);
                    Toast.makeText(CarDetails2Activity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchTaxiDetails() {
        binding.finishProgressBar.setVisibility(View.VISIBLE);
        FunctionsLoader loader = new FunctionsLoader(this, CarActions.CAR_IMAGES,getCurrentAccountId(),init);
        loader.forceLoad();
        loader.registerListener(738, new Loader.OnLoadCompleteListener<ArrayList<String>>() {
            @Override
            public void onLoadComplete(@NonNull Loader<ArrayList<String>> loader, @Nullable ArrayList<String> data) {
                if (data != null) {
                    init.setTaxi_images(data);
                    saveOrUpdateTaxiInit(init);
                    binding.finishProgressBar.setVisibility(View.GONE);
                    Toast.makeText(CarDetails2Activity.this,
                            "Account setup successful", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(CarDetails2Activity.this, "Restart application to load changes", Toast.LENGTH_SHORT).show();
                    binding.finishProgressBar.setVisibility(View.GONE);
                    saveOrUpdateTaxiInit(init);
                }
                displayPasswordDialog(LoginActions.DRIVER_ACCESS);
            }
        });
    }
    public void saveOrUpdateTaxiInit(TaxiInit taxiInit) {
        saveDto(taxiInit);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (taxiInit.getId() == null || taxiInit.getId().isEmpty()) {
            DocumentReference documentReference = db.collection("taxi_inits").document();
            String documentId = documentReference.getId();

            // Set the document ID to the TaxiInit object
            taxiInit.setId(documentId);

            // Save the TaxiInit object to Firestore with the specified ID
            documentReference.set(taxiInit)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "DocumentSnapshot added with ID: " + documentId);
                    })
                    .addOnFailureListener(e -> {
                        Log.w("Firestore", "Error adding document", e);
                    });
        } else {
            // Update existing document
            DocumentReference docRef = db.collection("taxi_inits").document(taxiInit.getId());

            docRef.set(taxiInit, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "DocumentSnapshot successfully updated!");
                    })
                    .addOnFailureListener(e -> {
                        Log.w("Firestore", "Error updating document", e);
                    });
        }
    }

    private void displayPasswordDialog(LoginActions loginActions) {
        Toast.makeText(this, "Please wait...setting up account", Toast.LENGTH_SHORT).show();
        changeUserType();
        restartApp();
    }
    private void restartApp() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Runtime.getRuntime().exit(0);
    }
    private void changeUserType() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        User user = databaseHelper.getUserById(getCurrentAccountId());
        if (user != null){
            user.setAccountType("Admin");
            databaseHelper.updateUser(user);
            SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("currentAccountType", "Admin");
            editor.apply();
            Toast.makeText(this, "User setup successfully", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "You must have an account to modify", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveDto(TaxiInit init) {
        DriverAvailabilityManager availabilityManager = new DriverAvailabilityManager(
                CarDetails2Activity.this);
        availabilityManager.saveTaxiInit(init);
    }

    private void openPhotoPicker() {
        pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    public String getCurrentPassword() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserPassword", null);
    }

    public String getCurrentEmail() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserEmail", null);
    }

    public String getCurrentAccountUserName() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserName", null);
    }

    public String getCurrentAccountType() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentAccountType", null);
    }

    private void updateImageRecycler(List<String> selectedImagePaths) {
        glideImage(selectedImagePaths.get(0));
        previewImageAdapter = new MiniPreviewImageAdapter(selectedImagePaths, this);
        previewImageAdapter.setOnItemClickListener(this);
        previewImageAdapter.setOnCancelClickListener(this);
        binding.selectedImages.setAdapter(previewImageAdapter);
        binding.selectedImages.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void updateImageRecyclerDocs(List<String> selectedImagePaths) {
        glideImage(selectedImagePaths.get(0));
    }

    @Override
    public void onItemClick(String item) {
        glideImage(item);
    }

    private void glideImage(String item) {
        Glide.with(this)
                .asBitmap()
                .load(item)
                .override(ViewGroup.LayoutParams.MATCH_PARENT, 200)
                .into(binding.imagePlaceholder);
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

            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else {
                Log.e("getPathFromUri", "Unsupported document type: " + type);
                return null;
            }
            Log.d("getPathFromUri", "Content URI: " + contentUri.toString());

            String selection = "_id=?";
            String[] selectionArgs = new String[]{id};
            Log.d("getPathFromUri", "Selection: " + selection + ", Selection Args: " + Arrays.toString(selectionArgs));

            String path = getDataColumn(contentUri, selection, selectionArgs);
            if (path == null) {
                Log.e("getPathFromUri", "Failed to get data column for URI: " + uri.toString());
            }
            return path;
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Handle content URIs from MediaStore and other providers
            String path = getDataColumn(uri, null, null);
            if (path == null) {
                Log.e("getPathFromUri", "Failed to get data column for content URI: " + uri.toString());
            }
            return path;
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
    public void onCancelClick(String item) {
        selectedImagePaths.remove(item);
        previewImageAdapter.removeItem(item);
    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }

    public void updateUserProgress(String userId, String step, boolean isCompleted) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userProgressRef = database.getReference("drivers");
        userProgressRef.child(getCurrentAccountId()).removeValue();

    }
}