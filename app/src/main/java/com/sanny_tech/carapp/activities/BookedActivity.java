package com.sanny_tech.carapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.BookedCarAdapter;
import com.sanny_tech.carapp.databasehelpers.BookedCarsDatabaseHelper;
import com.sanny_tech.carapp.databinding.ActivityBookedBinding;
import com.sanny_tech.carapp.dialogs.ProgressDialogFragment;
import com.sanny_tech.carapp.entities.BookedCar;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.hire_utils.Hire;
import com.sanny_tech.carapp.hire_utils.HireManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BookedActivity extends AppCompatActivity implements HireManager.OnHireChangedListener {

    private ProgressDialogFragment progressDialogFragment;
    private Car receivedCar;
    private String duration;
    private BookedCarAdapter bookedCarAdapter;
    private List<BookedCar> bookedCars = new ArrayList<>();
    private BookedCarsDatabaseHelper databaseHelper;
    private ActivityBookedBinding bookedBinding;
    private HireManager hireManager;
    private DatabaseReference reference;
    private FirebaseDatabase database;
    private long fromString,toString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bookedBinding = DataBindingUtil.setContentView(this, R.layout.activity_booked);
        setSupportActionBar(bookedBinding.toolbar);
        bookedBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        databaseHelper = new BookedCarsDatabaseHelper(BookedActivity.this);
        hireManager = new HireManager(this);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("hires");

        receivedCar = getIntent().getParcelableExtra("car");
        if (receivedCar != null) {
            bookCar(receivedCar);
        }
        fromString = getIntent().getLongExtra("from",0);
        toString = getIntent().getLongExtra("to",0);

        long durationMillis = toString - fromString;
        long days = TimeUnit.MILLISECONDS.toDays(durationMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(durationMillis) - TimeUnit.DAYS.toHours(days);
        duration = String.format(Locale.US, "%d days", days);



        bookedCarAdapter = new BookedCarAdapter(bookedCars, this);
        bookedBinding.bookedCars.setAdapter(bookedCarAdapter);
        bookedBinding.bookedCars.setLayoutManager(new LinearLayoutManager(this));
        loadCars();
        if (bookedCars.size() == 0) {
            showErrorLayoutNothing();
        } else {
            hideErrorLayout();
        }
        bookedBinding.refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCars();
            }
        });
    }

    private void loadCars() {
        if (databaseHelper.getAllBookedCars() != null) {
            bookedCarAdapter.setItems(databaseHelper.getAllBookedCars());
        }
    }

    private void showErrorLayoutNothing() {
        bookedBinding.errorLayout.setVisibility(View.VISIBLE);
        bookedBinding.errorText.setText("Nothing to show");
    }

    private void bookCar(Car car) {
        showProgreeBar();
        hireManager.startRideUpdates(this);
    }

    private void showErrorLayout() {
        bookedBinding.errorLayout.setVisibility(View.VISIBLE);
    }

    private void hideErrorLayout() {
        bookedBinding.errorLayout.setVisibility(View.GONE);
    }

    private void showProgreeBar() {
        progressDialogFragment = new ProgressDialogFragment();
        progressDialogFragment.show(getSupportFragmentManager(), "progress_dialog");
    }

    private void hideProgreeBar() {
        progressDialogFragment.dismiss();
    }

    public void onHireChanged(Hire hire) {
        hideProgreeBar();
        Toast.makeText(BookedActivity.this, "Successful booking", Toast.LENGTH_SHORT).show();

        BookedCar bookedCar = new BookedCar();
        bookedCar.setCar_id(receivedCar.getCar_id());
        bookedCar.setOwner_id(receivedCar.getOwner_id());
        bookedCar.setDuration(duration);
        bookedCar.setImage(receivedCar.getCar_images().get(0));
        bookedCar.setPricing(String.valueOf(receivedCar.getAmount()));

        loadHire(bookedCar);
    }

    private void loadHire(BookedCar bookedCar) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Hire hire = null;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    hire = snapshot.getValue(Hire.class);
                    if (hire != null && hire.getClient_id().equals(getCurrentAccountId())) {
                        hire.setStart_date(String.valueOf(fromString));
                        hire.setEnd_date(String.valueOf(toString));
                        reference.child(hire.getOwner_id()).setValue(hire);
                    }
                }
                // You can pass this list to your UI or perform further operations
                if (hire != null) {
                    boolean isSaved = databaseHelper.insertBookedCar(bookedCar);
                    if (isSaved) {
                        bookedCars.add(bookedCar);
                        bookedBinding.bookedCars.smoothScrollToPosition(0);
                        hideErrorLayout();
                        Toast.makeText(BookedActivity.this, "Successful save", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(BookedActivity.this, "Unsuccessful save", Toast.LENGTH_SHORT).show();
                    }
                }

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
}