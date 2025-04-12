package com.sanny_tech.carapp.activities;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.MiniPreviewImageAdapter;
import com.sanny_tech.carapp.adapters.OptionItemAdapter;
import com.sanny_tech.carapp.asynctasks.DatabaseAsyncTaskLoader;
import com.sanny_tech.carapp.databinding.ActivityAddDestinationBinding;
import com.sanny_tech.carapp.entities.AddressItem;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.entities.LatLngCustom;
import com.sanny_tech.carapp.entities.OptionItem;
import com.sanny_tech.carapp.enums.DatabaseAction;
import com.sanny_tech.carapp.fun_utils.OperatingHours;
import com.sanny_tech.carapp.fun_utils.SpaceDest;

import java.util.ArrayList;
import java.util.List;

public class AddDestinationActivity extends AppCompatActivity implements
        MiniPreviewImageAdapter.OnItemClickListener,
        MiniPreviewImageAdapter.OnCancelClickListener, OptionItemAdapter.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Boolean> {
    private static final int SELECT_LOCATION_REQUEST_CODE = 4;
    private ActivityAddDestinationBinding binding;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;
    private List<Uri> selectedFiles = new ArrayList<>();
    private List<String> selectedImagePaths = new ArrayList<>();
    private MiniPreviewImageAdapter previewImageAdapter;
    private int page_count = 0;
    private String owner,name,phone_number,alternate_number;
    private SpaceDest space;
    private List<String> selectedActivities,selectedServices;
    private List<OptionItem> itemList;
    private OptionItemAdapter adapter;
    private String category = "";
    private AddressItem addressItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_add_destination);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        ArrayList<String> selectedImages = new ArrayList<>();

// Registers a photo picker activity launcher in multi-select mode.
        pickMultipleMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        selectedImages.add(uri.toString());
                        selectedFiles.add(uri);
                        selectedImagePaths.add(uri.toString());
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                    updateImageRecycler(selectedImages);
                });
        itemList = new ArrayList<>();
        itemList.add(new OptionItem("Family Spaces", "Restraunts, Hotels, Accomodations, kids activities, Lodges"));
        itemList.add(new OptionItem("Adult Spaces", "Night clubs, bars"));

        adapter = new OptionItemAdapter(itemList);
        adapter.setOnItemClickListener(this);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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
        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePages();
            }
        });
        binding.nextButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (category.length() != 0){
                    binding.mainLt.setVisibility(View.VISIBLE);
                    binding.selectPlanLt.setVisibility(View.GONE);
                }else {
                    Toast.makeText(AddDestinationActivity.this, "No category selected!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void saveSpaceWithImage(SpaceDest funSpaceItem, Uri imageUri) {
        showProgressBar();
        Bundle bundle = new Bundle();
        bundle.putSerializable("action", DatabaseAction.SAVE);
        LoaderManager.getInstance(this).restartLoader(30, bundle, this);
    }
    private void handlePages() {
        if (page_count == 0) {
            name = binding.nameEdittext.getText().toString();
            owner = binding.ownerEdittext.getText().toString();
            phone_number = binding.numberEdittext.getText().toString();
            alternate_number = binding.altNumberEdittext.getText().toString();

            if (phone_number.length() == 0 || name.length() == 0) {
                Toast.makeText(this, "Fill in all the fields", Toast.LENGTH_SHORT).show();
            } else {
                space = new SpaceDest(String.valueOf(System.currentTimeMillis()),getCurrentAccountId(),
                        null,name,owner,phone_number,alternate_number);
                space.setCategory(category);
                binding.carDetails.setVisibility(View.GONE);
                binding.selectServices.getRoot().setVisibility(View.VISIBLE);
                page_count++;
            }
        } else if (page_count == 1) {
            selectedServices = getSelectedChips(binding.selectServices.chipGroupServicesOffered);
            selectedActivities = getSelectedChips(binding.selectServices.chipGroupGamesEntertainment);
            space.setSelectedActivities(selectedActivities);
            space.setSelectedServices(selectedServices);
            binding.selectServices.getRoot().setVisibility(View.GONE);
            binding.uploadLt.setVisibility(View.VISIBLE);
            page_count++;
            openSelectLocationActivity();
        } else if (page_count ==2) {
            showTimePickerDialog();
        } else if (page_count == 3) {
            if (selectedImagePaths.isEmpty()) {
                showSnackbar(binding.getRoot(), "You must select at least one image");
            } else {
//                    for (String image : selectedImagePaths) {
//                        File file = new File(image);
//                        selectedFiles.add(file);
//                    }
                saveSpaceWithImage(space,selectedFiles.get(0));
            }
        }
    }

    private void hideProgressBar() {
        binding.progressLt.setVisibility(View.GONE);
    }

    private void showProgressBar() {
        binding.progressLt.setVisibility(View.VISIBLE);
    }

    private void openPhotoPicker() {
        pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }
    private void showTimePickerDialog() {
        final int[] startHour = new int[1];
        final int[] startMinute = new int[1];

        TimePickerDialog startTimePicker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            startHour[0] = hourOfDay;
            startMinute[0] = minute;

            // Now show end time picker
            TimePickerDialog endTimePicker = new TimePickerDialog(this, (view1, hourOfDay1, minute1) -> {
                int endHour = hourOfDay1;
                int endMinute = minute1;

                // Set operating hours
                space.setOperatingHours(new OperatingHours(startHour[0], startMinute[0], endHour, endMinute));
                if (page_count == 2) {
                    page_count++;
                }
                handlePages();

            }, 0, 0, true);
            endTimePicker.setTitle("Select End Time");
            endTimePicker.show();

        }, 0, 0, true);
        startTimePicker.setTitle("Select Start Time");
        startTimePicker.show();
        startTimePicker.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (page_count == 2){
                    page_count++;
                }
            }
        });
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

    private void glideImage(String item) {
        Glide.with(this)
                .asBitmap()
                .load(item)
                .override(ViewGroup.LayoutParams.MATCH_PARENT, 200)
                .into(binding.imagePlaceholder);
    }

    private String getPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(projection[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }

    private void showSnackbar(View rootView, String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
    private List<String> getSelectedChips(ChipGroup chipGroup) {
        List<String> selectedChips = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedChips.add(chip.getText().toString());
            }
        }
        return selectedChips;
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
    @Override
    public void onItemClick(OptionItem item) {
        if (item.getTitle().equals("Family Spaces")){
            category = "Family";
        } else if (item.getTitle().equals("Adult Spaces")) {
            category = "Adult";
        }
    }
    private void openSelectLocationActivity() {
        Toast.makeText(this, "Please wait ...", Toast.LENGTH_SHORT).show();
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
                        Intent intent = new Intent(AddDestinationActivity.this, SelectLocationActivity.class);
                        // Set extra to indicate multiple selection mode if needed
                        intent.putExtra("isMultipleSelection", false);
                        intent.putExtra("activity","add_dest");
                        intent.putExtra("key", mapKey);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_LOCATION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            if (data.hasExtra("selectedAddress")) {
                addressItem = data.getParcelableExtra("selectedAddress");
                if (addressItem !=  null && space != null){
                    LatLngCustom lngCustom = new LatLngCustom(addressItem.getLatitude(),addressItem.getLongitude());
                    lngCustom.setName(addressItem.getAddress());
                    space.setLocation(lngCustom);
                }
            }
        }
    }
    @NonNull
    @Override
    public Loader<Boolean> onCreateLoader(int id, @Nullable Bundle args) {
        return new DatabaseAsyncTaskLoader(this,DatabaseAction.SAVE,
                space,selectedFiles);
    }
    @Override
    public void onLoadFinished(@NonNull Loader<Boolean> loader, Boolean data) {
        if (data) {
            hideProgressBar();
            Toast.makeText(this, "Operation successful!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Operation failed!", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onLoaderReset(@NonNull Loader<Boolean> loader) {

    }
}