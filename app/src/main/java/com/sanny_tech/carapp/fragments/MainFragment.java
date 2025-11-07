package com.sanny_tech.carapp.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.AboutCarActivity;
import com.sanny_tech.carapp.activities.FunSpacesActivity;
import com.sanny_tech.carapp.activities.ManageProfiles;
import com.sanny_tech.carapp.activities.MapsActivity;
import com.sanny_tech.carapp.activities.NotificationsActivity;
import com.sanny_tech.carapp.activities.ProfileActivity;
import com.sanny_tech.carapp.activities.RentingActivity;
import com.sanny_tech.carapp.activities.SearchActivity;
import com.sanny_tech.carapp.activities.SignInActivity;
import com.sanny_tech.carapp.adapters.AdsAdapter;
import com.sanny_tech.carapp.adapters.IconsAdapter;
import com.sanny_tech.carapp.adapters.QuickStepAdapter;
import com.sanny_tech.carapp.asynctasks.CarsRetrieverLoader;
import com.sanny_tech.carapp.asynctasks.ReviewLoader;
import com.sanny_tech.carapp.databasehelpers.UploadedCarsHelper;
import com.sanny_tech.carapp.databinding.DialogConfirmBinding;
import com.sanny_tech.carapp.databinding.FragmentMainBinding;
import com.sanny_tech.carapp.dialogs.MyBottomSheetDialog;
import com.sanny_tech.carapp.dialogs.WaitingListDialog;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.entities.Icon;
import com.sanny_tech.carapp.entities.Quickstep;
import com.sanny_tech.carapp.review.Review;
import com.sanny_tech.carapp.enums.ReviewAction;
import com.sanny_tech.carapp.storage.RemoteMessageSaver;
import com.sanny_tech.carapp.utils.CarUtils;
import com.sanny_tech.carapp.utils.DataCache;
import com.sanny_tech.carapp.utils.IpAddressManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Car>>,
        QuickStepAdapter.OnItemClickListener, IconsAdapter.OnItemClickListener, AdsAdapter.OnItemClickListener {
    private static final int REQUEST_CODE = 9;
    private FragmentMainBinding fragmentMainBinding;
    private Car car;
    private String baseUrl;
    private QuickStepAdapter quickStepAdapter;
    private List<Quickstep> quicksteps = new ArrayList<>();
    private List<Car> cars;
    private boolean isLoggedIn;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String profileImage;
    private List<Icon> icons;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double currentLatitude, currentLongitude;
    private List<Icon> iconList;
    private int LOCATION_PERMISSION_REQUEST_CODE = 334;

    public MainFragment() {
        // Required empty public constructor
    }
    // TODO: Rename and change types and number of parameters

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentMainBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        
        if (getArguments() != null) {
            isLoggedIn = getArguments().getBoolean("isLoggedIn", false);
            if (isLoggedIn) {
                if (getCurrentUserName() == null) {
                    showConfirmationDialog();
                } else {
                    if (getCurrentUserName().isEmpty()) {
                        showConfirmationDialog();
                    }
                }
            }
        }

        baseUrl = IpAddressManager.getIpAddress(requireContext());
        cars = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        icons = new ArrayList<>();
        Icon icon = new Icon(R.drawable.local_taxi, "Taxi");
        Icon rentalIcon = new Icon(R.drawable.car_rental, "Rent a car");
        Icon foodIcon = new Icon(R.drawable.streetfood_icon, "Food & drinks");
        Icon massageIcon = new Icon(R.drawable.massage_icon, "Massage");
        Icon bnbIcon = new Icon(R.drawable.concierge, "BnB");
        Icon funIcon = new Icon(R.drawable.celebration, "Fun Space");
        Icon allIcon = new Icon(R.drawable.apps, "All");

        icons.add(icon);
        icons.add(funIcon);
        icons.add(rentalIcon);
        icons.add(allIcon);

        IconsAdapter adapter = new IconsAdapter(requireContext(), icons);
        adapter.setOnItemClickListener(this);
        fragmentMainBinding.services.setLayoutManager(layoutManager);
        fragmentMainBinding.services.setAdapter(adapter);

        iconList = new ArrayList<>();
        Icon ad = new Icon(R.drawable.ad_2, "Family space");
        Icon ad2 = new Icon(R.drawable.ad_3, "Taxi");
        Icon ad3 = new Icon(R.drawable.ad_1, "Rental");

        iconList.add(ad);
        iconList.add(ad2);
        iconList.add(ad3);

        AdsAdapter adapter1 = new AdsAdapter(requireContext(), iconList);
        adapter1.setOnItemClickListener(this);
        fragmentMainBinding.adds.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL,false));
        fragmentMainBinding.adds.setAdapter(adapter1);
        updateNotificationDot();

        Quickstep quickstep = new Quickstep("rental_ad");
        Quickstep quickstep1 = new Quickstep("admin_ad");
        Quickstep quickstep2 = new Quickstep("rental_ad_1");
        quicksteps.add(quickstep);
        quicksteps.add(quickstep1);
        quicksteps.add(quickstep2);
        fragmentMainBinding.notificationLt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), NotificationsActivity.class);
                startActivity(intent);
            }
        });

//        fragmentMainBinding.searchBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openSearchActivity();
//            }
//        });

//        fragmentMainBinding.findCarButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                findACar();
//            }
//        });
        loadCars();

        return fragmentMainBinding.getRoot();

    }

    private void showSnackbar(View rootView, String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.blue));
        snackbar.setAction("Sign in", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), SignInActivity.class);
                startActivity(intent);
            }
        });
        snackbar.setAction("Cancel", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void showMaps() {
        Intent intent = new Intent(requireContext(), MapsActivity.class);
        startActivity(intent);
    }

    private void showSignInButton() {
        fragmentMainBinding.icon.setVisibility(View.GONE);
    }

    private void openSignInActivity() {
        Intent intent = new Intent(requireContext(), SignInActivity.class);
        startActivity(intent);
    }

    private void findACar() {
        MyBottomSheetDialog myBottomSheetDialog = new MyBottomSheetDialog();
        myBottomSheetDialog.show(getParentFragmentManager(), myBottomSheetDialog.getTag());
    }

    private void loadMostRequestedCar(String endpoint) {
        if (endpoint.matches("")) {
        } else {

        }
    }

    private void openProfileActivity() {
        Intent intent = new Intent(requireContext(), ProfileActivity.class);
        intent.setAction("profile");
        startActivity(intent);
    }

    private void openSearchActivity() {
        Intent intent = new Intent(requireContext(), SearchActivity.class);
        intent.setAction("search");
        startActivity(intent);

    }

    private void loadCars() {
        LoaderManager.getInstance(this).initLoader(1, null, this);
    }

    private void reloadCars() {
        LoaderManager.getInstance(this).restartLoader(1, null, this);
    }

    public String getCurrentUserId() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }

    private void showProgressBar() {
        fragmentMainBinding.progressLt.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        fragmentMainBinding.progressLt.setVisibility(View.GONE);
    }

    @NonNull
    @Override
    public Loader<List<Car>> onCreateLoader(int id, @Nullable Bundle args) {
        showProgressBar();
        return new CarsRetrieverLoader(requireContext());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Car>> loader, List<Car> data) {
        hideProgressBar();

        if (data != null && data.size() != 0) {
            addQuickstep(data);
            UploadedCarsHelper uploadedCarsHelper = new UploadedCarsHelper(requireContext());
            if (uploadedCarsHelper.getAllCars().isEmpty()) {
                Log.e("car loader", "upload started");
                List<Car> myCars = new ArrayList<>();
                for (Car car : data) {
                    if (getCurrentUserId() != null && !getCurrentUserId().isEmpty() &&
                            car.getOwner_id().matches(getCurrentUserId())) {
                        myCars.add(car);
                    }
                }
                for (Car car1 : myCars) {
                    uploadedCarsHelper.insertCar(car1);
                }
            }

        } else {
            if (DataCache.loadData(requireContext()) != null) {
                long lastUpdatedTime = DataCache.getLastUpdateTime(requireContext());
                if (lastUpdatedTime != 0) {
                    String formattedTime = formatTime(lastUpdatedTime); // Implement your time formatting method
                    fragmentMainBinding.timeText.setText("Last Updated: " + formattedTime);
                }
            } else {
            }
        }
        quickStepAdapter = new QuickStepAdapter(requireContext(), quicksteps);
        quickStepAdapter.setOnItemClickListener(this);
        fragmentMainBinding.quicksteps.setAdapter(quickStepAdapter);
        fragmentMainBinding.quicksteps.setLayoutManager(new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    private void addQuickstep(List<Car> data) {
        car = CarUtils.getRandomCar(data);

        String endPoint = baseUrl + "/car/image/" + car.getOwner_id() + "/"
                + car.getCar_id() + "/" + car.getCar_images().get(0);
        double amount = car.getDaily_amount();
        Locale kenyanLocale = new Locale("sw", "KE");
        Currency kenyanShilling = Currency.getInstance("KES");
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(kenyanLocale);
        numberFormat.setCurrency(kenyanShilling);
        String formattedAmount = numberFormat.format(amount);
        Quickstep carQuickstep = new Quickstep(car.getCar_id(),endPoint, car.getModel(),car.getDescription(),
                car.getLocation(),formattedAmount);
        if (!quicksteps.contains(carQuickstep)) {
            quicksteps.add(0,carQuickstep);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Car>> loader) {

    }

    private String getWallPaper() {
        // Get a reference to SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);

        return sharedPreferences.getString("profilePic", null);
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        DialogConfirmBinding dialogConfirmBinding = DataBindingUtil.inflate(getLayoutInflater(),
                R.layout.dialog_confirm, null, false);
        builder.setView(dialogConfirmBinding.getRoot());

        dialogConfirmBinding.dialogMessage.setText("Do you want to continue configuring your account?");

        final AlertDialog dialog = builder.create();

        dialogConfirmBinding.dialogConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the user's choice to continue configuring the account
                dialog.dismiss();
                if (getArguments() != null) {
                    getArguments().clear();
                }
                openAboutAccount();
            }
        });

        dialogConfirmBinding.dialogCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the user's choice to cancel
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void openAboutAccount() {
        Intent intent = new Intent(requireContext(), ManageProfiles.class);
        intent.putExtra("instruction", "configure");
        startActivity(intent);
    }

    public String getCurrentUserName() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserName", null);
    }

    private void showReviewBottomSheet(Car item) {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_add_review, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        bottomSheetDialog.setContentView(bottomSheetView);

        // Find views in the bottom sheet layout
        RatingBar ratingBar = bottomSheetView.findViewById(R.id.ratingBar);
        TextInputLayout feedbackTextInputLayout = bottomSheetView.findViewById(R.id.feedbackTextInputLayout);
        TextInputEditText feedbackEditText = bottomSheetView.findViewById(R.id.comment);
        MaterialButton submitButton = bottomSheetView.findViewById(R.id.submitButton);

        submitButton.setOnClickListener(v -> {
            // Get feedback and rating inputs
            String comment = feedbackEditText.getText().toString();
            float rating = ratingBar.getRating();
            Review review = new Review(getCurrentUserId(), item.getCar_id(), item.getOwner_id(),
                    "", comment, rating, "");

            ReviewLoader reviewLoader = new ReviewLoader(requireContext(), car, review, ReviewAction.CREATE);
            reviewLoader.forceLoad();

            reviewLoader.registerListener(7, new Loader.OnLoadCompleteListener<Object>() {
                @Override
                public void onLoadComplete(@NonNull Loader<Object> loader, @Nullable Object data) {
                    if (data != null) {
                        bottomSheetDialog.dismiss();
                        Toast.makeText(requireContext(), "Review submitted successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Review not submitted.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            // Perform your submission logic here
            // You can send the feedback and rating to a server or store them locally
            // Then dismiss the bottom sheet
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener((Activity) requireContext(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();

                        }
                    }
                });
    }

    public String getCurrentAccountType() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentAccountType", null);
    }

    private void updateNotificationDot() {
        // Call hasUnreadMessages() to check for unread messages
        boolean hasUnread = RemoteMessageSaver.hasUnreadMessages(requireContext()); // Implement this method
        fragmentMainBinding.notificationDot.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(Quickstep item) {
        if (getCurrentUserId() != null) {
            if (item.getId().equals("rental_ad") || item.getId().equals("rental_ad_1")){
                WaitingListDialog.show(requireContext(), "Rent a car", getCurrentUserId());
            }else if (item.getId().equals("admin_ad")){
                NavController navController = Navigation.findNavController(fragmentMainBinding.getRoot());
                navController.navigate(R.id.extrasFragment);
            }else {
                if (item.getId().equals(car.getCar_id())) {
                    Intent intent = new Intent(requireContext(), AboutCarActivity.class);
                    intent.putExtra("selectedCar", car);
                    startActivity(intent);
                    requireActivity().overridePendingTransition(R.anim.scale_up, R.anim.scale_down);
                }
            }
        } else {
            showSnackbar(fragmentMainBinding.getRoot(),
                    "You must have an account to use these services.");
        }
    }

    @Override
    public void onItemClick(Icon item) {
        if (getCurrentUserId() != null && !getCurrentUserId().equals("")) {
            if (item.getDesc().matches("Rent a car")) {
                Intent intent = new Intent(requireContext(), RentingActivity.class);
                startActivity(intent);
//                WaitingListDialog.show(requireContext(), item.getDesc(), getCurrentUserId());
            } else if (item.getDesc().matches("Taxi")) {
                Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show();
                if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE);
                }else {
                    loadApi();
                }
            } else if (item.getDesc().matches("Fun Space")) {
                showFunSpaceDialog();
            }else if (item.getDesc().matches("All")) {
                displayAllServicesDialog();
            } else if (item.getDesc().matches("Family space")) {
                Intent intent = new Intent(requireContext(), FunSpacesActivity.class);
                intent.putExtra("category","Family");
                startActivity(intent);
            }
//            else if (item.getSubtext().matches("Massage")) {
//                Intent intent = new Intent(requireContext(), MassageActivity.class);
//                startActivity(intent);
//            }
            else {
                WaitingListDialog.show(requireContext(), item.getDesc(), getCurrentUserId());
            }
        } else {
            showSnackbar(fragmentMainBinding.getRoot(),
                    "You must have an account to use these services.");
        }
    }

    private void loadApi() {
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
                        Intent intent = new Intent(requireContext(), MapsActivity.class);
                        intent.putExtra("key", mapKey);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void showFunSpaceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        DialogConfirmBinding dialogConfirmBinding = DataBindingUtil.inflate(getLayoutInflater(),
                R.layout.dialog_confirm, null, false);
        builder.setView(dialogConfirmBinding.getRoot());

        dialogConfirmBinding.dialogMessage.setText(
                "What would you like to explore? ");

        final AlertDialog dialog = builder.create();

        dialogConfirmBinding.dialogConfirmButton.setText("Family Space");
        dialogConfirmBinding.dialogCancelButton.setText("Clubs & Events");
        dialogConfirmBinding.dialogConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the user's choice to continue configuring the account
                dialog.dismiss();
                Intent intent = new Intent(requireContext(), FunSpacesActivity.class);
                intent.putExtra("category","Family");
                startActivity(intent);
            }
        });

        dialogConfirmBinding.dialogCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the user's choice to cancel
                dialog.dismiss();
                Intent intent = new Intent(requireContext(), FunSpacesActivity.class);
                intent.putExtra("category","Adult");
                startActivity(intent);
            }
        });
        dialog.setCancelable(true);

        dialog.show();
    }
    private void displayAllServicesDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_all_services);

        RecyclerView services = dialog.findViewById(R.id.services);
        List<Icon> iconList = new ArrayList<>();
        Icon icon = new Icon(R.drawable.local_taxi, "Taxi");
        Icon rentalIcon = new Icon(R.drawable.car_rental, "Rent a car");
        Icon foodIcon = new Icon(R.drawable.streetfood_icon, "Food & drinks");
        Icon massageIcon = new Icon(R.drawable.massage_icon, "Massage");
        Icon bnbIcon = new Icon(R.drawable.concierge, "BnB");
        Icon funIcon = new Icon(R.drawable.celebration, "Fun Space");
        Icon allIcon = new Icon(R.drawable.apps, "All");

        iconList.add(icon);
        iconList.add(funIcon);
        iconList.add(rentalIcon);
        iconList.add(bnbIcon);
        iconList.add(foodIcon);
        iconList.add(massageIcon);

        IconsAdapter adapter = new IconsAdapter(requireContext(), iconList);
        adapter.setOnItemClickListener(this);
        services.setLayoutManager(new GridLayoutManager(requireContext(),3));
        services.setAdapter(adapter);

        ImageButton imageButton = dialog.findViewById(R.id.back);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationCustom;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

}