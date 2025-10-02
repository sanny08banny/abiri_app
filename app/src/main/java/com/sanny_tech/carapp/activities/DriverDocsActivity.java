package com.sanny_tech.carapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.DriverDocsAdapter;
import com.sanny_tech.carapp.adapters.MiniImageAdapter;
import com.sanny_tech.carapp.asynctasks.FilesUploadLoader;
import com.sanny_tech.carapp.asynctasks.FunctionsLoader;
import com.sanny_tech.carapp.databinding.ActivityDriverDocsBinding;
import com.sanny_tech.carapp.enums.CarActions;
import com.sanny_tech.carapp.enums.UploadActions;
import com.sanny_tech.carapp.guides.CarDetails2Activity;
import com.sanny_tech.carapp.guides.CarDetailsActivity;
import com.sanny_tech.carapp.guides.IdentitiesUploadActivity;
import com.sanny_tech.carapp.guides.NewDriverGuideActivity;
import com.sanny_tech.carapp.taxi_utils.TaxiInit;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverDocsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<ArrayList<String>>, DriverDocsAdapter.OnItemClickListener, MiniImageAdapter.OnItemClickListener {
    private ActivityDriverDocsBinding binding;
    private DriverDocsAdapter driverAdapter;
    private String driver_id;
    private List<String> docs = new ArrayList<>();
    private ActivityResultLauncher<PickVisualMediaRequest> pickSingleMedia;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;
    private List<String> selectedImagePaths = new ArrayList<>();
    private List<Uri> selectedFiles = new ArrayList<>();
    private List<TaxiInit> myInits;
    private String selectedDoc;
    private String instruction;
    private TaxiInit init;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_driver_docs);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        pickSingleMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                selectedImagePaths.clear();
                selectedFiles.clear();
                selectedFiles.add(uri);
                selectedImagePaths.add(uri.toString());
            } else {
                Log.d("PhotoPicker", "No media selected");
            }
            uploadDoc();
        });
        pickMultipleMedia =
                registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(5), uris -> {
                    // Callback is invoked after the user selects media items or closes the
                    // photo picker.
                    if (!uris.isEmpty()) {
                        Log.d("PhotoPicker", "Number of items selected: " + uris.size());
                        selectedImagePaths.clear();
                        selectedFiles.clear();
                        // Convert URIs to strings and add them to the selectedImages list
                        for (Uri uri : uris) {
                            selectedFiles.add(uri);
                            selectedImagePaths.add(uri.toString());
                        }
                        uploadDoc();
                        // Now, selectedImages contains the list of selected image URIs
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                        if (instruction != null){
                            finish();
                        }
                    }
                });
        instruction = getIntent().getStringExtra("instruction");
        init = getIntent().getParcelableExtra("init");
        if (instruction != null){
            openPhotoPickerMultiple();
        }
        if (init.getTaxi_images() != null && !init.getTaxi_images().isEmpty()){
            MiniImageAdapter miniImageAdapter = new MiniImageAdapter(init.getTaxi_images(),this);
            miniImageAdapter.setOnItemClickListener(this);
            binding.carRecyclerView.setAdapter(miniImageAdapter);
            binding.carRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                    LinearLayoutManager.HORIZONTAL,false));
        }
        getAllTaxiInitForUser(getCurrentAccountId());
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        driver_id = getCurrentAccountId();
        driverAdapter = new DriverDocsAdapter(docs, this, driver_id);
        driverAdapter.setOnItemClickListener(this);
        binding.recyclerView.setAdapter(driverAdapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadDocs();
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reLoadDocs();
            }
        });
    }

    private void loadDocs() {
        hideErrorLayout();
        showProgressBar();
        LoaderManager.getInstance(this).initLoader(48, null, this);
    }
    private void reLoadDocs() {
        hideErrorLayout();
        showProgressBar();
        LoaderManager.getInstance(this).restartLoader(48, null, this);
    }

    @NonNull
    @Override
    public Loader<ArrayList<String>> onCreateLoader(int id, @Nullable Bundle args) {
        return new FunctionsLoader(this, CarActions.FETCH_PENDING_DOCS, driver_id, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<String>> loader, ArrayList<String> data) {
        hideProgressBar();
        binding.swipeRefreshLayout.setRefreshing(false);
        if (data != null) {
            driverAdapter.setItems(data,null);
            fetchVerifiedDocs();
        } else {
            showErrorLayout();
        }
    }

    private void fetchVerifiedDocs() {
        FunctionsLoader functionsLoader1 = new FunctionsLoader(this,CarActions.FETCH_UNVERIFIED_DOCS,
                getCurrentAccountId(),null);
        functionsLoader1.forceLoad();
        functionsLoader1.registerListener(5, new Loader.OnLoadCompleteListener<ArrayList<String>>() {
            @Override
            public void onLoadComplete(@NonNull Loader<ArrayList<String>> loader, @Nullable ArrayList<String> data) {
                if (data != null){
                    driverAdapter.setItems(null,data);
                }
            }
        });
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<String>> loader) {

    }

    private void showProgressBar() {
        binding.progressLt.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        binding.progressLt.setVisibility(View.GONE);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void showErrorLayout() {
        binding.errorLayout.setVisibility(View.VISIBLE);
    }

    private void hideErrorLayout() {
        binding.errorLayout.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(String item) {
        selectedDoc = item;
        if (selectedDoc.equals("NationalId")) {
            showInstructionDialog();
        } else if (selectedDoc.equals("DrivingLicense")) {
            showInstructionDialog();
        } else {
            openPhotoPicker();
        }
    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }

    private void openPhotoPicker() {
        pickSingleMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void showInstructionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Instructions")
                .setMessage("Please select the front and back images.")
                .setPositiveButton("OK", (dialog, which) -> openPhotoPickerMultiple())
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void uploadDoc() {
        if (selectedDoc != null) {
            if (selectedDoc.equals("NationalId")) {
                if (selectedImagePaths.size() == 2) {
                    uploadNationalId(selectedImagePaths);
                }else {
                    Toast.makeText(this, "Select only front and back",
                            Toast.LENGTH_SHORT).show();
                }
            } else if (selectedDoc.equals("DrivingLicense")) {
                if (selectedImagePaths.size() == 2) {
                    uploadLicence(selectedImagePaths);
                }else {
                    Toast.makeText(this, "Select only front and back",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                if (selectedImagePaths.size() == 1) {
                    binding.progressLt1.setVisibility(View.VISIBLE);
                    binding.progressTitle.setText("Uploading");
                    File file = new File(getPathFromUri(selectedFiles.get(0)));
                    String desc = "null";
                    if (selectedDoc.equals("Insurance")) {
                        desc = "insurance";
                    } else if (selectedDoc.equals("InspectionReport")) {
                        desc = "inspection_report";
                    } else if (selectedDoc.equals("PsvLicense")) {
                        desc = "psv_license";
                    }
                    Map<String, File> files = new HashMap<>();
                    files.put(desc, file);
                    FilesUploadLoader filesUploadLoader = new FilesUploadLoader(this, null,
                            files, UploadActions.UPLOAD_ONE_DOC, myInits.get(0), null);
                    filesUploadLoader.forceLoad();
                    filesUploadLoader.registerListener(7, new Loader.OnLoadCompleteListener<String>() {
                        @Override
                        public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                            if (data != null) {
                                updateUserProgress(getCurrentAccountId(), "licence", true);
                                updateUserProgress(getCurrentAccountId(), "psv_badge", true);
                                binding.progressLt1.setVisibility(View.GONE);
                                showSnackbar(binding.getRoot(), "Document Upload\n" +
                                        selectedDoc + " has been uploaded successfully");
                                selectedImagePaths.clear();
                                selectedFiles.clear();
                                binding.swipeRefreshLayout.setRefreshing(true);
                                reLoadDocs();
                            } else {
                                Toast.makeText(DriverDocsActivity.this, "Upload failed",
                                        Toast.LENGTH_SHORT).show();
                                binding.progressLt1.setVisibility(View.GONE);
                            }
                        }
                    });
                }else {
                    Toast.makeText(this, "Select only one image", Toast.LENGTH_SHORT).show();
                }
            }
        }else {
            if (instruction != null){
                uploadCarImages();
            }
        }
    }

    private void showSnackbar(View rootView, String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.green));
        snackbar.setAction("Change", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        snackbar.show();
    }

    public void updateUserProgress(String userId, String step, boolean isCompleted) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userProgressRef = database.getReference("drivers").child(userId);

        userProgressRef.child(step).setValue(isCompleted).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Handle success
                Log.d("RealtimeDatabase", "Step " + step + " " +
                        "updated successfully for user " + userId);
            } else {
                // Handle failure
                Log.w("RealtimeDatabase", "Error updating step " + step + " for user " + userId, task.getException());
            }
        });
    }
    public void getAllTaxiInitForUser(String userId) {
        myInits = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("taxi_inits")
                .whereEqualTo("driver_id", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                TaxiInit taxiInit = document.toObject(TaxiInit.class);
                                myInits.add(taxiInit);
                                // Handle each TaxiInit object
                                Log.e("Firestore", "TaxiInit: " + taxiInit.toString());
                            }
                            if (myInits.get(0) != null) {
                            } else {
                                Intent intent = new Intent(DriverDocsActivity.this, NewDriverGuideActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Log.e("FireStore", "Not found");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error getting documents.", e);
                    }
                });
    }

    private void uploadNationalId(List<String> selectedImagePaths) {
        binding.progressLt1.setVisibility(View.VISIBLE);
        File idFrontFile = new File(getPathFromUri(selectedFiles.get(0)));
        File idBackFile = new File(getPathFromUri(selectedFiles.get(1)));
        Map<String, File> files = new HashMap<>();
        files.put("national_id_front", idFrontFile);
        files.put("national_id_back", idBackFile);
        FilesUploadLoader filesUploadLoader = new FilesUploadLoader(this, null,
                files, UploadActions.UPLOAD_DOCS, myInits.get(0), null);
        filesUploadLoader.forceLoad();
        filesUploadLoader.registerListener(7, new Loader.OnLoadCompleteListener<String>() {
            @Override
            public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                if (data != null) {
                    binding.progressLt1.setVisibility(View.GONE);
                    Toast.makeText(DriverDocsActivity.this, "National id uploaded successfully", Toast.LENGTH_SHORT).show();
                    selectedFiles.clear();
                    selectedImagePaths.clear();
                    recreate();
                } else {
                    Toast.makeText(DriverDocsActivity.this, "National id upload failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadLicence(List<String> selectedImagePaths) {
        binding.progressLt1.setVisibility(View.VISIBLE);
        binding.progressTitle.setText("Uploading");
        File idFrontFile = new File(getPathFromUri(selectedFiles.get(0)));
        File idBackFile = new File(getPathFromUri(selectedFiles.get(1)));
        Map<String, File> files = new HashMap<>();
        files.put("driving_license_front", idFrontFile);
        files.put("driving_license_back", idBackFile);
        FilesUploadLoader filesUploadLoader = new FilesUploadLoader(this, null,
                files, UploadActions.UPLOAD_DOCS3, myInits.get(0), null);
        filesUploadLoader.forceLoad();
        filesUploadLoader.registerListener(7, new Loader.OnLoadCompleteListener<String>() {
            @Override
            public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                if (data != null) {
                    binding.progressLt1.setVisibility(View.GONE);
                    showSnackbar(binding.getRoot(), "Document Upload\n" +
                            "Documents have been uploaded successfully");
                    selectedFiles.clear();
                    selectedImagePaths.clear();
                    recreate();
                } else {
                    Toast.makeText(DriverDocsActivity.this, "Failed",
                            Toast.LENGTH_SHORT).show();
                    recreate();
                }
            }
        });
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
    private void openPhotoPickerMultiple() {
        pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }
    private void uploadCarImages() {
        instruction = "";
        List<File> files = new ArrayList<>();
        for (Uri uri : selectedFiles) {
            File idFrontFile = new File(getPathFromUri(uri));
            files.add(idFrontFile);
        }
        Toast.makeText(this, "Uploading images", Toast.LENGTH_SHORT).show();
        binding.progressLt1.setVisibility(View.VISIBLE);

        FilesUploadLoader filesUploadLoader = new FilesUploadLoader(this, null,
                null, UploadActions.UPLOAD_IMAGES, init, files);
        filesUploadLoader.forceLoad();
        filesUploadLoader.registerListener(7, new Loader.OnLoadCompleteListener<String>() {
            @Override
            public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                if (data != null) {
                    binding.progressLt1.setVisibility(View.GONE);
                    Toast.makeText(DriverDocsActivity.this,
                            "Image has been uploaded successfully. Restart app to load changes", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    binding.progressLt1.setVisibility(View.GONE);
                    Toast.makeText(DriverDocsActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}