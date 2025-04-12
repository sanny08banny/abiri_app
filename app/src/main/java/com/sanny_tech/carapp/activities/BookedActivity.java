package com.sanny_tech.carapp.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.BookedCarAdapter;
import com.sanny_tech.carapp.databinding.ActivityBookedBinding;
import com.sanny_tech.carapp.databinding.DialogAboutBookingBinding;
import com.sanny_tech.carapp.dialogs.ProgressDialogFragment;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.hire_utils.Hire;
import com.sanny_tech.carapp.hire_utils.HireManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sanny_tech.carapp.utils.IpAddressManager;

public class BookedActivity extends AppCompatActivity{

    private ProgressDialogFragment progressDialogFragment;
    private Car receivedCar;
    private String duration;
    private BookedCarAdapter bookedCarAdapter;
    private ActivityBookedBinding bookedBinding;
    private HireManager hireManager;
    private DatabaseReference reference;
    private FirebaseDatabase database;
    private long fromString, toString;
    private FirebaseAnalytics mFirebaseAnalytics;
    private String baseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bookedBinding = DataBindingUtil.setContentView(this, R.layout.activity_booked);
        setSupportActionBar(bookedBinding.toolbar);
        bookedBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        baseUrl = IpAddressManager.getIpAddress(this);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        bookedBinding.refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCars();
            }
        });
    }

    private void loadCars() {
    }

    private void showErrorLayoutNothing() {
        bookedBinding.errorLayout.setVisibility(View.VISIBLE);
        bookedBinding.errorText.setText("Nothing to show");
    }

    private void showErrorLayout() {
        bookedBinding.errorLayout.setVisibility(View.VISIBLE);
    }

    private void hideErrorLayout() {
        bookedBinding.errorLayout.setVisibility(View.GONE);
    }
    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs",
                MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }

    private void showAboutBookedCarDialog(Hire car) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        DialogAboutBookingBinding dialogAboutBookingBinding = DataBindingUtil.inflate(inflater,R.layout.dialog_about_booking,
                null,false);
        View dialogView = inflater.inflate(R.layout.dialog_about_booking, null);
        dialogBuilder.setView(dialogView);

//        if (car.getImage() != null) {
//            String image = car.getImage();
//            String endPoint = baseUrl + "/car/" + car.getOwner_id() + "/"
//                    + car.getCar_id() + "/" + car.getImage();
//            Glide.with(this)
//                    .asBitmap()
//                    .load(endPoint)
//                    .apply(new RequestOptions()
//                            .placeholder(R.drawable.baseline_downloading_350) // Placeholder image while loading
//                            .error(R.drawable.car_01)      // Error image if loading fails
//                            .diskCacheStrategy(DiskCacheStrategy.ALL))
//                    .override(ViewGroup.LayoutParams.MATCH_PARENT, 500)
//                    .into(dialogAboutBookingBinding.imageView);
//        }
        
//        dialogAboutBookingBinding.description.setText(car.getDuration());

        dialogBuilder.setTitle("Create Delivery Option");
        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing on cancel
            }
        });
        AlertDialog b = dialogBuilder.create();

        // Apply the animation to the dialog
        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_up_dialog);
        dialogView.startAnimation(scaleAnimation);

        b.show();
    }

}