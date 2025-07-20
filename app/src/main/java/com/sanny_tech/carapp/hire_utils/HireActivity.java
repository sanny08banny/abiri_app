package com.sanny_tech.carapp.hire_utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.databinding.DataBindingUtil;
import androidx.loader.content.Loader;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.AddPhoneNumberActivity;
import com.sanny_tech.carapp.activities.ManageProfiles;
import com.sanny_tech.carapp.asynctasks.BookCarLoader;
import com.sanny_tech.carapp.databinding.ActivityHireBinding;
import com.sanny_tech.carapp.dialogs.ProgressDialogFragment;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.enums.ActionType;
import com.sanny_tech.carapp.utils.IpAddressManager;
import com.sanny_tech.carapp.utils.SimCardManager;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HireActivity extends AppCompatActivity implements
        HireManager.OnHireChangedListener {

    private static final int ADD_NEW_NUMBER = 30;
    private static final int SETUP_ACCOUNT = 34;
    private FirebaseDatabase database;
    private ActivityHireBinding hireBinding;
    private Car receivedCar;
    private DatabaseReference reference;
    private HireManager hireManager;
    private long fromString, toString;
    private String duration;
    private ProgressDialogFragment progressDialogFragment;
    private String baseUrl;
    private Hire hire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hireBinding = DataBindingUtil.setContentView(this, R.layout.activity_hire);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        setSupportActionBar(hireBinding.aboutSchoolToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        handleUserData();
        baseUrl = IpAddressManager.getIpAddress(this);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("hires");

        receivedCar = getIntent().getParcelableExtra("car");
        if (receivedCar != null) {
            glideImage(receivedCar, hireBinding.carImage);
            hireBinding.model.setText(receivedCar.getModel());
        }
        fromString = getIntent().getLongExtra("from", 0);
        toString = getIntent().getLongExtra("to", 0);
        hireBinding.date.setText(MessageFormat.format("{0} - {1}",
                formatTime(fromString), formatTime(toString)));

        long durationMillis = toString - fromString;
        long days = TimeUnit.MILLISECONDS.toDays(durationMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(durationMillis) - TimeUnit.DAYS.toHours(days);
        duration = String.format(Locale.US, "%d days", days);
        hireBinding.priceDetails.setText(receivedCar.getDaily_amount() + " * " + duration);
        double totalPrice = days * receivedCar.getDaily_amount();
        hireBinding.totalPrice.setText("KSH " + totalPrice);
        hireBinding.totalPrice2.setText("KSH " + totalPrice);

        String descriptionText = getString(R.string.edit);

        // Find the index of the clickable text "[CHANGE PREFERENCES]"
        int changePreferencesStartIndex = descriptionText.indexOf("EDIT");

        // Only proceed if the clickable text is found in the original string
        if (changePreferencesStartIndex != -1) {
            SpannableString spannableString = new SpannableString(descriptionText);

            // Create a ClickableSpan for the clickable text
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    // Perform your action here, e.g., open the recommended activity
                    showDatePickerDialog(HireActivity.this, receivedCar);
                }
            };

            // Set the ClickableSpan to the part of the text that needs to be clickable
            spannableString.setSpan(clickableSpan, changePreferencesStartIndex, changePreferencesStartIndex + "EDIT".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Set the modified SpannableString to the TextView
            hireBinding.editDate.setText(spannableString);
            hireBinding.editDate.setMovementMethod(LinkMovementMethod.getInstance());
        }
        hireBinding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadHire();
                hireManager = new HireManager(HireActivity.this, receivedCar.getCar_id());
            }
        });

    }

    private void handleUserData() {
        if (getCurrentAccountId() != null && getCurrentAccountUserName() != null) {
            hireBinding.userDetails.setVisibility(View.VISIBLE);
            hireBinding.userName.setText(getCurrentAccountUserName());
            if (!SimCardManager.getPhoneNumber(this).equals("")) {
                hireBinding.phoneNumber.setText(SimCardManager.getPhoneNumber(this));
            } else {
                hireBinding.addButton.setVisibility(View.VISIBLE);
                hireBinding.addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openPhoneNumberActivity();
                    }
                });
            }
        } else {
            hireBinding.setupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }

    private void loadHire() {
        showProgreeBar();
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            Hire activeHire;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Hire hire;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        hire = snapshot.getValue(Hire.class);
                        if (hire != null && hire.getClient_id().equals(getCurrentAccountId()) &&
                                hire.getCarId().equals(receivedCar.getCar_id()) &&
                                hire.getStart_date().matches(String.valueOf(fromString))) {
                            hideProgreeBar();
                            Toast.makeText(HireActivity.this, "Successful booking. Check your activity", Toast.LENGTH_SHORT).show();
                        } else {
                            bookCar(receivedCar);
                        }
                    }
                } else {
                    bookCar(receivedCar);
                }
                // You can pass this list to your UI or perform further operations

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });

    }

    private void bookCar(Car car) {
        BookCarLoader bookCarLoader = new BookCarLoader(this, car.getOwner_id(),
                ActionType.BOOK, car);
        bookCarLoader.forceLoad();
        bookCarLoader.registerListener(7, new Loader.OnLoadCompleteListener<String>() {
            @Override
            public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                String id = car.getOwner_id() + "_" + getCurrentAccountId() +
                        "_" + System.currentTimeMillis();
                Hire hire = new Hire(id, car.getOwner_id(), getCurrentAccountId(),
                        (float) car
                                .getDaily_amount(), car.getCar_id(), "initialised", "",
                        SimCardManager.getPhoneNumber(HireActivity.this),
                        "", getCurrentAccountUserName());
                hire.setCar(car);
                hire.setStart_date(String.valueOf(fromString));
                hire.setEnd_date(String.valueOf(toString));
                if (data != null) {
                    createNewHireToFirebase(hire);
                    hireManager.startRideUpdates(HireActivity.this);
                } else {
                    hideProgreeBar();
                    Toast.makeText(HireActivity.this, "Unsuccessful booking",
                            Toast.LENGTH_SHORT).show();
//                    FirebaseMessaging.getInstance().subscribeToTopic(hire.getId())
//                            .addOnCompleteListener(task -> {
//                                if (!task.isSuccessful()) {
//                                    Log.w("FCM", "Subscription failed");
//                                } else {
//                                    Log.d("FCM", "Subscribed to hireId topic: " + hire.getId());
//                                }
//                            });
                    hire.setStatus("local-request");
                    reference.child(hire.getId()).setValue(hire);
                }
            }
        });
    }

    private void createNewHireToFirebase(Hire hire) {
        reference.child(hire.getId()).setValue(hire);
        Toast.makeText(this, "Hew hire created successful. Wait for owner response.", Toast.LENGTH_SHORT).show();
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void showProgreeBar() {
        progressDialogFragment = new ProgressDialogFragment();
        progressDialogFragment.show(getSupportFragmentManager(), "progress_dialog");
    }

    private void hideProgreeBar() {
        progressDialogFragment.dismiss();
    }

    public void onHireChanged(Hire hire) {
        if (hire.getCarId().equals(receivedCar.getCar_id()) &&
                hire.getStart_date().matches(String.valueOf(fromString))) {
            if (hire.getStatus().equals("verified")) {
                hideProgreeBar();
                Toast.makeText(HireActivity.this, "Successful booking. Check your activity", Toast.LENGTH_SHORT).show();
                hireManager.stopHireUpdates();
                finish();
            }
        }
    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs",
                MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }

    private void glideImage(Car car, ImageView imageView) {
        if (car != null) {
            String endPoint = baseUrl + "/car/image/" + car.getOwner_id() + "/"
                    + car.getCar_id() + "/" + car.getCar_images().get(0);
            Glide.with(this).load(endPoint)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.baseline_downloading_350) // Placeholder image while loading
                            .error(R.drawable.baseline_downloading_350)      // Error image if loading fails
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .override(ViewGroup.LayoutParams.MATCH_PARENT, 500)
                    .into(imageView);
        }
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
                fromString = selection.first;
                toString = selection.second;

                // Convert milliseconds to a duration string
                long durationMillis = toString - fromString;
                long days = TimeUnit.MILLISECONDS.toDays(durationMillis);
                long hours = TimeUnit.MILLISECONDS.toHours(durationMillis) - TimeUnit.DAYS.toHours(days);
                duration = String.format(Locale.US, "%d days", days);

                hireBinding.priceDetails.setText(receivedCar.getDaily_amount() + " * " + duration);
                double totalPrice = days * receivedCar.getDaily_amount();
                hireBinding.totalPrice.setText("KSH " + totalPrice);
                hireBinding.totalPrice2.setText("KSH " + totalPrice);

                hireBinding.date.setText(MessageFormat.format("{0} - {1}",
                        formatTime(fromString), formatTime(toString)));
            }
        });


        picker.show(((AppCompatActivity) context).getSupportFragmentManager(), picker.toString());
    }

    public String getCurrentAccountUserName() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserName", null);
    }

    private void openManageAccountActivity() {
        Intent intent = new Intent(HireActivity.this, ManageProfiles.class);
        startActivityForResult(intent, SETUP_ACCOUNT);
    }

    private void openPhoneNumberActivity() {
        Intent intent = new Intent(HireActivity.this, AddPhoneNumberActivity.class);
        intent.putExtra("instruction", "verification");
        startActivityForResult(intent, ADD_NEW_NUMBER);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        handleUserData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NEW_NUMBER && resultCode == RESULT_OK && data != null) {
            String selectedNo = data.getStringExtra("selectedNo");
        } else if (requestCode == SETUP_ACCOUNT) {

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Handle back button click
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}