package com.sanny_tech.carapp.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.SnapHelper;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.DestinationsAdapter;
import com.sanny_tech.carapp.adapters.PreviewImageAdapter;
import com.sanny_tech.carapp.asynctasks.FunSpaceLoader;
import com.sanny_tech.carapp.databinding.ActivityCreateFunSpaceBinding;
import com.sanny_tech.carapp.entities.FunSpace;
import com.sanny_tech.carapp.enums.FunActions;
import com.sanny_tech.carapp.fun_utils.SpaceDest;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateFunSpaceActivity extends AppCompatActivity implements DestinationsAdapter.OnItemClickListener{
    private ActivityCreateFunSpaceBinding funSpaceBinding;

    private DatabaseReference mDatabase;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;
    private PreviewImageAdapter previewImageAdapter;
    private List<String> selectedImagePaths = new ArrayList<>();
    private List<Uri> selectedFiles = new ArrayList<>();
    private DestinationsAdapter destinationsAdapter;
    private List<SpaceDest> dests = new ArrayList<>();
    private SpaceDest destination;
    private String category;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        funSpaceBinding = DataBindingUtil.setContentView(this, R.layout.activity_create_fun_space);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        mDatabase = FirebaseDatabase.getInstance().getReference("fun_spaces");

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
                            selectedFiles.add(uri);
                            selectedImagePaths.add(uri.toString());
                        }
                        updateImageRecycler(selectedImages);
                        // Now, selectedImages contains the list of selected image URIs
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });
        calendar = Calendar.getInstance();

        funSpaceBinding.editTextExpiryDate.setOnClickListener(v -> {
            new DatePickerDialog(this, date, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
        destinationsAdapter = new DestinationsAdapter(this,dests);
        funSpaceBinding.destsRecycler.setAdapter(destinationsAdapter);
        funSpaceBinding.destsRecycler.setLayoutManager(new LinearLayoutManager(this));
        destinationsAdapter.setOnItemClickListener(this);
        getAvailableDests();
        funSpaceBinding.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        funSpaceBinding.addImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPhotoPicker();
            }
        });
        funSpaceBinding.submitHouse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String desc = funSpaceBinding.descriptionEdittext.getText().toString();
                if (selectedImagePaths == null) {
                    showSnackbar(funSpaceBinding.getRoot(), "You must select at least one image");
                } else {
                    if (!funSpaceBinding.editTextExpiryDate.getText().equals("Select Expiry Date")) {
                        saveFunSpaceItemWithImages(
                                new FunSpace("", getCurrentAccountId(),
                                        null, desc, null, 0, 0), selectedFiles);
                    }
                }
            }
        });
        funSpaceBinding.addDest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateFunSpaceActivity.this, AddDestinationActivity.class);
                startActivity(intent);
            }
        });
    }
    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
    // Method to save a FunSpaceItem with images to the Realtime Database
    private void saveFunSpaceItemWithImages(FunSpace funSpaceItem, List<Uri> imageUris) {
        showProgressBar();
        if (destination != null){
            funSpaceItem.setDestination(destination);
        }
        funSpaceItem.setExpiry_date(String.valueOf(calendar.getTime()));
        FunSpaceLoader spaceLoader = new FunSpaceLoader(this, FunActions.SAVE,
                funSpaceItem,imageUris,null);
        spaceLoader.forceLoad();
        spaceLoader.registerListener(3, new Loader.OnLoadCompleteListener<List<FunSpace>>() {
            @Override
            public void onLoadComplete(@NonNull Loader<List<FunSpace>> loader, @Nullable List<FunSpace> data) {
                if (data == null){
                    hideProgressBar();
                    Toast.makeText(CreateFunSpaceActivity.this, "FunSpaceItem with images saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void openPhotoPicker() {
        pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                .build());
    }

    private void updateImageRecycler(ArrayList<String> selectedImagePaths) {
        previewImageAdapter = new PreviewImageAdapter(selectedImagePaths, this);
        funSpaceBinding.imagesRecycler.setAdapter(previewImageAdapter);
        funSpaceBinding.imagesRecycler.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(funSpaceBinding.imagesRecycler);
    }

    private void showSnackbar(View rootView, String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
    public void getAvailableDests() {
        // This method retrieves all available taxis nearby within a certain distance

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("destinations");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<SpaceDest> availableDests = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    SpaceDest spaceDest = snapshot.getValue(SpaceDest.class);
                    if (spaceDest != null & spaceDest.getOwner_id().equals(getCurrentAccountId())) {
                        availableDests.add(spaceDest);
                    }
                }

                if (!availableDests.isEmpty()){
                    destinationsAdapter.setItems(availableDests);
                }else {
                    Intent intent = new Intent(CreateFunSpaceActivity.this, AddDestinationActivity.class);
                    startActivity(intent);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
    }

    @Override
    public void onItemClick(SpaceDest item) {
        funSpaceBinding.destLt.setVisibility(View.VISIBLE);
        funSpaceBinding.paymentType.setText(item.getName());
        Glide.with(this)
                .load(item.getImages_urls().get(0))
                .into(funSpaceBinding.image);
        destination = item;
    }
    private void hideProgressBar() {
        funSpaceBinding.progressBar.setVisibility(View.GONE);
        funSpaceBinding.submitHouse.setVisibility(View.VISIBLE);
    }

    private void showProgressBar() {
        funSpaceBinding.progressBar.setVisibility(View.VISIBLE);
        funSpaceBinding.submitHouse.setVisibility(View.GONE);
    }
    private DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            updateLabel();
        }
    };

    private void updateLabel() {
        String myFormat = "yyyy-MM-dd"; // Your date format
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        funSpaceBinding.editTextExpiryDate.setText(sdf.format(calendar.getTime()));
    }
}