package com.sanny_tech.carapp.guides;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.loader.content.Loader;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.MiniPreviewImageAdapter;
import com.sanny_tech.carapp.asynctasks.FilesUploadLoader;
import com.sanny_tech.carapp.databinding.ActivityCarDetailsBinding;
import com.sanny_tech.carapp.enums.UploadActions;
import com.sanny_tech.carapp.taxi_utils.TaxiInit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarDetailsActivity extends AppCompatActivity {
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 100;
    private static final int PICK_DOCUMENT_FILE = 5;
    private static final int PICK_IMAGE_REQUEST = 766;
    private ActivityCarDetailsBinding binding;
    private long page_count = 0;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;
    private List<Uri> selectedFiles = new ArrayList<>();
    private List<String> selectedImagePaths = new ArrayList<>();
    private MiniPreviewImageAdapter previewImageAdapter;
    private int defaultColor;
    private List<TaxiInit> myInits = new ArrayList<>();
    private boolean isProgressed = false;
    private List<File> selectedDocsFiles = new ArrayList<>();
    private long maxSize = 3 * 1024 * 1024;
    int maxWidth = 1024;
    int maxHeight = 768;
    private ActivityResultLauncher<String[]> requestPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_car_details);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        TaxiInit init = getIntent().getParcelableExtra("init");
        if (init != null) {
            myInits.add(init);
        }
        fetchUserProgress(getCurrentAccountId());
        String text = "Add National ID(front)";
        updateUI(text, "This document will be used according to our terms and conditions." +
                " Its sole aim is to help us identify you as a certified driver for our" +
                " customers safety.", "This document will be used according to our terms and conditions." +
                " Its sole aim is to help us identify you as a certified driver for our" +
                " customers safety.");

        requestPermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), results -> {
            boolean allGranted = true;
            for (Boolean granted : results.values()) {
                if (!granted) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                openPhotoPicker();  // Permissions granted, open the photo picker
            } else {
                Toast.makeText(this, "Permissions required to pick photos.", Toast.LENGTH_SHORT).show();
                openBackupImagePicker();  // Open backup picker if permissions are denied
            }
        });
        pickMultipleMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                if (page_count == 0 || page_count == 3) {
                    if (!selectedImagePaths.isEmpty()) {
                        selectedImagePaths.clear();
                        selectedFiles.clear();
                    }
                }
                if (page_count == 1 || page_count == 4) {
                    if (selectedImagePaths.size() >= 2) {
                        selectedImagePaths.remove(1);
                        selectedFiles.remove(1);
                    }
                }
                selectedFiles.add(uri);
                selectedImagePaths.add(uri.toString());
            } else {
                Log.d("PhotoPicker", "No media selected");
            }
            if (!selectedImagePaths.isEmpty()) {
                updateImageRecyclerDocs(selectedImagePaths);
            }else {
                Log.d("PhotoPicker", "No image picked or operation cancelled.");
            }

        });
        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (page_count != 2) {
                    page_count++;
                    handlePages();
                }
            }
        });
        binding.uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPicker();
            }
        });
        binding.imagePlaceholder2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPicker();
            }
        });
        binding.previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImagePaths != null) {
                    if (page_count == 0) {
                        selectedImagePaths.clear();
                        glideImage2("");
                    } else if (page_count == 1) {
                        if (selectedImagePaths.size() == 2) {
                            selectedImagePaths.remove(1);
                            glideImage2("");
                        }
                    } else if (page_count == 3) {
                        selectedImagePaths.clear();
                        selectedFiles.clear();
                        glideImage2("");
                    } else if (page_count == 4) {
                        selectedImagePaths.remove(1);
                        selectedFiles.remove(1);
                        glideImage2("");
                    }
                }
            }
        });
    }

    private void handlePages() {
        if (page_count == 1) {
            if (!selectedImagePaths.isEmpty()) {
                glideImage2("");
                String text = "Add National ID(back)";
                updateUI(text, "This document will be used according to our terms and conditions." +
                        " Its sole aim is to help us identify you as a certified driver for our" +
                        " customers safety.", "This document will be used according to our terms and conditions." +
                        " Its sole aim is to help us identify you as a certified driver for our" +
                        " customers safety.");
            } else {
                page_count--;
                Toast.makeText(this, "No image selected1", Toast.LENGTH_SHORT).show();
            }
        } else if (page_count == 2) {
            if (selectedImagePaths.size() < 2 && !isProgressed) {
                page_count--;
                Toast.makeText(this, "No image selected2", Toast.LENGTH_SHORT).show();
            } else {
                if (!isProgressed) {
                    uploadNationalId(selectedImagePaths);
                }
            }
        } else if (page_count == 3) {
            selectedImagePaths.clear();
            selectedFiles.clear();
            glideImage2("");
            updateUI("Add Driving License(front)", "Confirm you are a certified driver",
                    "This document will be used according to our terms and conditions." +
                            " Its sole aim is to help us identify you as a certified driver for our" +
                            " customers safety."
            );
        } else if (page_count == 4) {
            if (selectedImagePaths.isEmpty()) {
                page_count--;
                Toast.makeText(this, "No image selected3", Toast.LENGTH_SHORT).show();
            } else {
                glideImage2("");
                updateUI("Add Driving License(back)", "Confirm you are a certified driver",
                        "This document will be used according to our terms and conditions." +
                                " Its sole aim is to help us identify you as a certified driver for our" +
                                " customers safety."
                );
            }
        } else if (page_count == 5) {
            if (selectedImagePaths.size() == 2) {
                uploadLicence(selectedImagePaths);
            } else {
                Intent intent = new Intent(CarDetailsActivity.this,
                        CarDetails2Activity.class);
                intent.putExtra("init", myInits.get(0));
                startActivity(intent);
            }
        } else if (page_count < 0) {
            finish();
        }
    }

    private void uploadLicence(List<String> selectedImagePaths) {
        binding.progressLt.setVisibility(View.VISIBLE);
        binding.progressTitle.setText("Uploading");
        String frontPath = getPathFromUri(selectedFiles.get(1));
        String backPath = getPathFromUri(selectedFiles.get(1));
        File idFrontFile = new File(frontPath);
        File idBackFile = new File(backPath);
        if (idFrontFile.length() > maxSize || idBackFile.length() > maxSize) {
            binding.progressLt.setVisibility(View.GONE);
            Toast.makeText(this, "File size exceeds limit", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Resize or compress the front image if necessary
            File compressedFrontFile = reduceResolution(frontPath, maxWidth, maxHeight, maxSize);
            // Log final size in MBs
            Log.d("UploadNationalId", "Compressed Front File Size: " + compressedFrontFile.length() / (1024.0 * 1024.0) + " MB");

            // Resize or compress the back image if necessary
            File compressedBackFile = reduceResolution(frontPath, maxWidth, maxHeight, maxSize);
            // Log final size in MBs
            Log.d("UploadNationalId", "Compressed Back File Size: " + compressedBackFile.length() / (1024.0 * 1024.0) + " MB");

            // Use the compressed files for further processing or upload
            idFrontFile = compressedFrontFile;
            idBackFile = compressedBackFile;

        } catch (IOException e) {
            e.printStackTrace();
            binding.progressLt.setVisibility(View.GONE);
            Toast.makeText(this, "Error processing image files", Toast.LENGTH_SHORT).show();
        }
        Map<String, File> files = new HashMap<>();
        files.put("driving_license_front", idFrontFile);
        files.put("driving_license_back", idBackFile);
        FilesUploadLoader filesUploadLoader = new FilesUploadLoader(this, null,
                files, UploadActions.UPLOAD_DOCS2, myInits.get(0), null);
        filesUploadLoader.forceLoad();
        filesUploadLoader.registerListener(87, new Loader.OnLoadCompleteListener<String>() {
            @Override
            public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                if (data != null) {
                    updateUserProgress(getCurrentAccountId(), "licence", true);
                    binding.progressLt.setVisibility(View.GONE);
                    showSnackbar(binding.getRoot(), "Document Upload\n" +
                            "Documents have been uploaded successfully");
                    finish();
                    Intent intent = new Intent(CarDetailsActivity.this,
                            CarDetails2Activity.class);
                    intent.putExtra("init", myInits.get(0));
                    startActivity(intent);
                } else {

                }
            }
        });
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

    private void updateImageRecyclerDocs(List<String> selectedImagePaths) {
        if (page_count == 0) {
            glideImage2(selectedImagePaths.get(0));
        } else if (page_count == 1) {
            glideImage2(selectedImagePaths.get(1));
        } else if (page_count == 3) {
            glideImage2(selectedImagePaths.get(0));
        } else if (page_count == 4) {
            glideImage2(selectedImagePaths.get(1));
        }
    }

    private void openPicker() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Select Files")
//                .setItems(new CharSequence[]{"Images", "Documents"}, (dialog, which) -> {
//                    if (which == 0) {
//                        // Open Image Picker
//                        openPhotoPicker();
//                    } else if (which == 1) {
//                        // Open Document Picker
//                        openDocumentPicker();
//                    }
//                });
//        builder.create().show();
        openPhotoPicker();
    }

    private void openDocumentPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // To allow all types of files, change MIME type as needed
        String[] mimeTypes = {"application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, PICK_DOCUMENT_FILE);
    }
    private void openBackupImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void openPhotoPicker() {
        try {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
                requestPermissions.launch(new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO
                });
            } else {
                requestPermissions.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
            }

            // Check for Google Play services availability or fallback if necessary
            if (isGooglePlayServicesAvailable()) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build());
                } else {
                    openBackupImagePicker();
                }
            } else {
                openBackupImagePicker();  // Fallback if Google Play services are unavailable
            }
        } catch (Exception e) {
            Log.e("PhotoPicker", "Error opening photo picker: " + e.getMessage(), e);
            Toast.makeText(this, "An error occurred while opening the photo picker.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            // If the request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openPicker();
            } else {
//                openBackupImagePicker();
                // Permission was denied, show an error message or handle accordingly
                // Optionally, you could display a message explaining why the permission is necessary
            }
        }
    }

    private void uploadNationalId(List<String> selectedImagePaths) {
        binding.progressLt.setVisibility(View.VISIBLE);

        // Log the selected files and their paths
        Log.d("UploadNationalId", "Selected Files: " + selectedFiles);
        File idFrontFile = null;
        File idBackFile = null;
        if (!selectedFiles.isEmpty()) {
            String frontPath = getPathFromUri(selectedFiles.get(0));
            String backPath = getPathFromUri(selectedFiles.get(1));
            Log.d("UploadNationalId", "Front Path: " + frontPath);
            Log.d("UploadNationalId", "Back Path: " + backPath);

            // Check if paths are valid
            if (frontPath == null || backPath == null) {
                binding.progressLt.setVisibility(View.GONE);
                Toast.makeText(this, "Invalid file path", Toast.LENGTH_SHORT).show();
                return;
            }

            idFrontFile = new File(frontPath);
            idBackFile = new File(backPath);
            if (idFrontFile.length() > maxSize || idBackFile.length() > maxSize) {
                Toast.makeText(this, "File size exceeds limit. Compressing...", Toast.LENGTH_SHORT).show();
            }

            try {
                // Resize or compress the front image if necessary
                File compressedFrontFile = reduceResolution(frontPath, maxWidth, maxHeight, maxSize);
                // Log final size in MBs
                Log.d("UploadNationalId", "Compressed Front File Size: " + compressedFrontFile.length() / (1024.0 * 1024.0) + " MB");

                // Resize or compress the back image if necessary
                File compressedBackFile = reduceResolution(frontPath, maxWidth, maxHeight, maxSize);
                // Log final size in MBs
                Log.d("UploadNationalId", "Compressed Back File Size: " + compressedBackFile.length() / (1024.0 * 1024.0) + " MB");

                // Use the compressed files for further processing or upload
                idFrontFile = compressedFrontFile;
                idBackFile = compressedBackFile;

                // Proceed with upload or other operations
                // ...
//                completeIdUpload(idFrontFile,idBackFile);

            } catch (IOException e) {
                e.printStackTrace();
                binding.progressLt.setVisibility(View.GONE);
                Toast.makeText(this, "Error processing image files", Toast.LENGTH_SHORT).show();
            }
        } else {
            idFrontFile = selectedDocsFiles.get(0);
            idBackFile = selectedDocsFiles.get(1);
        }
        completeIdUpload(idFrontFile, idBackFile);
    }

    private void completeIdUpload(File idFrontFile, File idBackFile) {
        Map<String, File> files = new HashMap<>();
        files.put("national_id_front", idFrontFile);
        files.put("national_id_back", idBackFile);

        FilesUploadLoader filesUploadLoader = new FilesUploadLoader(this, null,
                files, UploadActions.UPLOAD_DOCS, myInits.get(0), null);

        // Ensure the loader is correctly initialized
        Log.d("UploadNationalId", "FilesUploadLoader initialized");

        filesUploadLoader.forceLoad();
        filesUploadLoader.registerListener(7, new Loader.OnLoadCompleteListener<String>() {
            @Override
            public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                binding.progressLt.setVisibility(View.GONE);

                // Log the result
                Log.d("UploadNationalId", "Load Complete: " + data);

                if (data != null) {
                    page_count++;
                    handlePages();
                    Toast.makeText(CarDetailsActivity.this, "National id uploaded successfully", Toast.LENGTH_SHORT).show();
                    updateUserProgress(getCurrentAccountId(), "national_id", true);
                } else {
                    Toast.makeText(CarDetailsActivity.this, "National id upload failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void glideImage2(String item) {
        Glide.with(this)
                .asBitmap()
                .load(item)
                .override(ViewGroup.LayoutParams.MATCH_PARENT, 400)
                .into(binding.imagePlaceholder2);
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

    private void updateUI(String text, String title2, String bottomText) {
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new UnderlineSpan(), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.underlinedTextView.setText(spannableString);
        binding.title2.setText(title2);
        binding.bottomText.setText(bottomText);
    }

    public void fetchUserProgress(String userId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userProgressRef = database.getReference("drivers").child(userId);

        userProgressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Parse the user progress data
                    Map<String, Boolean> userProgress = new HashMap<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String step = snapshot.getKey();
                        Boolean isCompleted = snapshot.getValue(Boolean.class);
                        userProgress.put(step, isCompleted);
                        if (step != null) {
                            if (step.equals("national_id")) {
                                page_count++;
                                page_count++;
                                page_count++;
                                isProgressed = true;
                            }
                            if (step.equals("licence")) {
                                finish();
                                Intent intent = new Intent(CarDetailsActivity.this,
                                        CarDetails2Activity.class);
                                intent.putExtra("init", myInits.get(0));
                                startActivity(intent);
                            }
                        }
                    }
                    handlePages();
                } else {
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_DOCUMENT_FILE) {
                if (resultData != null) {
                    Uri uri = resultData.getData();
                    if (uri != null) {
                        // Handle the selected document URI
                        String documentType = getContentResolver().getType(uri);
                        String documentName = getDocumentName(uri);
                        binding.underlinedTextView.setText(documentName);
                        Glide.with(this)
                                .load(getDrawableResIdForMimeType(
                                        documentType
                                ))
                                .into(binding.imagePlaceholder2);
                        String path = getPathFromUri(uri); // Your method to get the path from URI
                        Log.d("DocumentPicker", "Selected document path: " + path);
                        // You can now use the path to upload or process the document
                        if (page_count == 0 || page_count == 3) {
                            if (!selectedImagePaths.isEmpty()) {
                                selectedImagePaths.clear();
                                selectedFiles.clear();
                                selectedDocsFiles.clear();
                            }
                        }
                        if (page_count == 1 || page_count == 4) {
                            if (selectedImagePaths.size() >= 2) {
                                selectedImagePaths.remove(1);
                                selectedFiles.remove(1);
                            }
                        }
                        selectedDocsFiles.add(getFileFromUri(uri));
                        selectedImagePaths.add(uri.toString());
                        if (!selectedImagePaths.isEmpty() && !selectedFiles.isEmpty()) {
                            updateImageRecyclerDocs(selectedImagePaths);
                        }
                    }
                }
            }else if (requestCode == PICK_IMAGE_REQUEST && resultData != null && resultData.getData() != null) {
                Uri uri = resultData.getData();
                if (uri != null) {
                    if (page_count == 0 || page_count == 3) {
                        if (!selectedImagePaths.isEmpty()) {
                            selectedImagePaths.clear();
                            selectedFiles.clear();
                        }
                    }
                    if (page_count == 1 || page_count == 4) {
                        if (selectedImagePaths.size() >= 2) {
                            selectedImagePaths.remove(1);
                            selectedFiles.remove(1);
                        }
                    }
                    selectedFiles.add(uri);
                    selectedImagePaths.add(uri.toString());
                } else {
                    Log.d("PhotoPicker", "No media selected");
                }
                if (!selectedImagePaths.isEmpty()) {
                    updateImageRecyclerDocs(selectedImagePaths);
                }
            }
        }
    }

    private String getDocumentName(Uri uri) {
        String displayName = null;
        if (uri != null) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0 && cursor.moveToFirst()) {
                        displayName = cursor.getString(nameIndex);
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return displayName != null ? displayName : "Unknown";
    }

    private String getMimeType(Uri uri) {
        String mimeType = null;
        if (uri != null) {
            ContentResolver contentResolver = getContentResolver();
            mimeType = contentResolver.getType(uri);
        }
        return mimeType;
    }

    public static int getDrawableResIdForMimeType(String mimeType) {
        if (mimeType == null) {
            return R.drawable.icon_empty; // Default icon if MIME type is unknown
        }
        switch (mimeType) {
            case "application/pdf":
                return R.drawable.pdf;
            case "application/msword":
                return R.drawable.word;
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return R.drawable.word; // Use same icon for .docx
            case "application/vnd.ms-excel":
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                return R.drawable.excel;
            default:
                return R.drawable.icon_empty; // Icon for unknown MIME types
        }
    }

    private File getFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                File file = createTempFile();
                FileOutputStream outputStream = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                inputStream.close();
                outputStream.close();

                return file;
            }
        } catch (IOException e) {
            Log.e("FileHandler", "Error reading file from URI: " + e.getMessage(), e);
        }
        return null;
    }

    private File createTempFile() throws IOException {
        File tempFile = File.createTempFile("tempFile", ".tmp", getCacheDir());
        tempFile.deleteOnExit();
        return tempFile;
    }

    private File reduceResolution(String path, int maxWidth, int maxHeight, long maxSize) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        if (bitmap == null) {
            throw new IOException("Failed to decode image");
        }

        // Create a temporary file for the compressed image
        File compressedFile = new File(getCacheDir(), "compressed_" + new File(path).getName());

        try (FileOutputStream out = new FileOutputStream(compressedFile)) {
            // Compress the image with 80% quality
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);  // Adjust quality if needed
        }

        // Check if the file size is within the limit
        if (compressedFile.length() > maxSize) {
            throw new IOException("Compressed file size exceeds limit");
        }

        return compressedFile;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        return resultCode == ConnectionResult.SUCCESS;
    }
}