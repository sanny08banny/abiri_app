package com.sanny_tech.carapp.fun_utils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.MapsActivity;
import com.sanny_tech.carapp.adapters.ChipAdapter;
import com.sanny_tech.carapp.adapters.FunSpacesAdapter;
import com.sanny_tech.carapp.adapters.PropertyAdapter;
import com.sanny_tech.carapp.asynctasks.DatabaseAsyncTaskLoader;
import com.sanny_tech.carapp.databinding.ActivityAbouFunSpaceBinding;
import com.sanny_tech.carapp.entities.AddressItem;
import com.sanny_tech.carapp.entities.FunSpace;
import com.sanny_tech.carapp.entities.OptionItem;
import com.sanny_tech.carapp.enums.DatabaseAction;
import com.sanny_tech.carapp.taxi_utils.FirebaseHelper;
import com.sanny_tech.carapp.viewPagers.ImagePagerAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AboutFunSpaceActivity extends AppCompatActivity {
    private ActivityAbouFunSpaceBinding binding;
    private SpaceDest spaceDest;
    private ImagePagerAdapter adapter;
    private FunSpacesAdapter funSpacesAdapter;
    private List<FunSpace> spaceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_abou_fun_space);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        binding.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        spaceDest = getIntent().getParcelableExtra("space");
        if (spaceDest != null){
            if (getCurrentAccountId() != null){
                if (spaceDest.getOwner_id().equals(getCurrentAccountId())){
                    binding.deleteLi.setVisibility(View.VISIBLE);
                }
            }
            binding.name.setText(spaceDest.getName());
            ArrayList<String> images = new ArrayList<>(spaceDest.getImages_urls());
            adapter = new ImagePagerAdapter(this, images);
            binding.houseImagesViewPager.setAdapter(adapter);
            binding.houseImagesViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    updatePageIndicator(position);
                }
            });

            // Initialize the indicator text
            updatePageIndicator(0);
            if (spaceDest.getLocation() != null) {
                binding.location.setText(spaceDest.getLocation().getName());
            }
            if (spaceDest.getSelectedServices() != null){
                ChipAdapter chipAdapter = new ChipAdapter(spaceDest.getSelectedServices());
                binding.servicesRecycler.setAdapter(chipAdapter);
                binding.servicesRecycler.setLayoutManager(new GridLayoutManager(this,
                        2));
            }
            List<OptionItem> optionItems = new ArrayList<>();
                OptionItem optionItem = new OptionItem(spaceDest.getCategory(),spaceDest.getOwner_name());
                optionItems.add(optionItem);
            if (spaceDest.getOperatingHours() != null){
                OperatingHours operatingHours = spaceDest.getOperatingHours();
                String formattedHours = String.format("%02d:%02d - %02d:%02d",
                        operatingHours.getStartHour(), operatingHours.getStartMinute(),
                        operatingHours.getEndHour(), operatingHours.getEndMinute());
                OptionItem optionItem1 = new OptionItem("Working Hours",formattedHours);
                optionItems.add(optionItem1);
            }
            PropertyAdapter propertyAdapter = new PropertyAdapter(optionItems,this);
            binding.propertiesRecycler.setAdapter(propertyAdapter);
            binding.propertiesRecycler.setLayoutManager(new LinearLayoutManager(this));

            funSpacesAdapter = new FunSpacesAdapter(this,spaceList);
            binding.eventsRecycler.setAdapter(funSpacesAdapter);
            binding.eventsRecycler.setLayoutManager(new LinearLayoutManager(this));
            getFunSpaces();
        }
        binding.deleteCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSpace();
            }
        });
        binding.getRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AboutFunSpaceActivity.this, "Fetching location", Toast.LENGTH_SHORT).show();
                FirebaseHelper firebaseHelper = new FirebaseHelper(new FirebaseHelper.MapKeyCallback() {
                    @Override
                    public void onMapKeyReceived(String mapKey) {
                        if (spaceDest.getLocation() != null) {
                            Log.d("latitude", String.valueOf(spaceDest.getLocation().getLatitude()));
                            AddressItem item = new AddressItem(spaceDest.getLocation().getName(),0,
                                    spaceDest.getLocation().getLatitude(),
                                    spaceDest.getLocation().getLongitude());
                            Intent intent = new Intent(AboutFunSpaceActivity.this,
                                    MapsActivity.class);
                            intent.putExtra("key", mapKey);
                            intent.putExtra("item", item);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onError(DatabaseError error) {

                    }
                });
                firebaseHelper.fetchMapKey();
            }
        });
    }

    private void deleteSpace() {
        Toast.makeText(this, "Removing...", Toast.LENGTH_SHORT).show();
        DatabaseAsyncTaskLoader taskLoader = new DatabaseAsyncTaskLoader(this,
                DatabaseAction.DELETE,spaceDest,null);
        taskLoader.forceLoad();
        taskLoader.registerListener(4, new Loader.OnLoadCompleteListener<Boolean>() {
            @Override
            public void onLoadComplete(@NonNull Loader<Boolean> loader, @Nullable Boolean data) {
                if (data != null){
                    Toast.makeText(AboutFunSpaceActivity.this, "Event removed successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void updatePageIndicator(int position) {
        int total = adapter.getItemCount();
        binding.count.setText((position + 1) + "/" + total);
    }
    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
    public void getFunSpaces() {
        // This method retrieves all available fun spaces and deletes expired ones

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("fun_spaces");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<FunSpace> funSpaceList = new ArrayList<>();
                List<String> expiredKeys = new ArrayList<>();

                // Get the current date
                Date currentDate = new Date();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FunSpace funSpace = snapshot.getValue(FunSpace.class);
                    if (funSpace != null && funSpace.getDestination() != null &&
                            funSpace.getDestination().getId().equals(spaceDest.getId())) {

                        // Parse expiry_date and check if it is not expired
                        String expiryDateStr = funSpace.getExpiry_date();
                        if (expiryDateStr != null) {
                            try {
                                // Assuming expiry_date is in a standard format such as "yyyy-MM-dd'T'HH:mm:ss"
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                                Date expiryDate = dateFormat.parse(expiryDateStr);
                                if (expiryDate != null && currentDate.before(expiryDate)) {
                                    funSpaceList.add(funSpace);
                                } else {
                                    expiredKeys.add(funSpace.getId()); // Collect keys of expired items
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                                // Handle parsing error if any
                            }
                        }
                    }
                }

                // Delete expired items from the database
                for (String key : expiredKeys) {
                    reference.child(key).removeValue();
                }

                // Update the adapter with non-expired items
                if (!funSpaceList.isEmpty()) {
                    funSpacesAdapter.setItems(funSpaceList);
                } else {
                    Toast.makeText(AboutFunSpaceActivity.this, "No events", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
    }

}