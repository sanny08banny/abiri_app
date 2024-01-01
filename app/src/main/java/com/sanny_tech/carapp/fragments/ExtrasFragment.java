package com.sanny_tech.carapp.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.AddCarActivity;
import com.sanny_tech.carapp.activities.BookedActivity;
import com.sanny_tech.carapp.activities.MapsActivity;
import com.sanny_tech.carapp.activities.SignInActivity;
import com.sanny_tech.carapp.adapters.BookedCarAdapter;
import com.sanny_tech.carapp.adapters.CustomSpinnerAdapter;
import com.sanny_tech.carapp.adapters.UploadedCarAdapter;
import com.sanny_tech.carapp.asynctasks.ProfileFetchRunnable;
import com.sanny_tech.carapp.databasehelpers.BookedCarsDatabaseHelper;
import com.sanny_tech.carapp.databasehelpers.UploadedCarsHelper;
import com.sanny_tech.carapp.databinding.FragmentExtrasBinding;
import com.sanny_tech.carapp.databinding.PasswordInputBinding;
import com.sanny_tech.carapp.entities.BookedCar;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.enums.LoginActions;
import com.sanny_tech.carapp.hire_utils.Hire;
import com.sanny_tech.carapp.utils.NewAppManager;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExtrasFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExtrasFragment extends Fragment{
    private FragmentExtrasBinding extrasBinding;
    private BookedCarAdapter bookedCarAdapter;
    private List<BookedCar> bookedCars = new ArrayList<>();
    private List<Car> uploadedCars = new ArrayList<>();
    private UploadedCarAdapter uploadedCarAdapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private BookedCarsDatabaseHelper databaseHelper;
    private PasswordInputBinding passwordInputBinding;
    private boolean isDriverOptionSelected;
    private DatabaseReference reference;

    public ExtrasFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ExtrasFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExtrasFragment newInstance(String param1, String param2) {
        ExtrasFragment fragment = new ExtrasFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

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
        extrasBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_extras, container, false);
        databaseHelper = new BookedCarsDatabaseHelper(requireContext());

        if (databaseHelper.getAllBookedCars() != null) {
            bookedCars = databaseHelper.getAllBookedCars();
        }
        reference = FirebaseDatabase.getInstance().getReference("hires"); // Replace with your Firebase location reference
        bookedCarAdapter = new BookedCarAdapter(bookedCars,requireContext());
        extrasBinding.bookedCars.setAdapter(bookedCarAdapter);
        extrasBinding.bookedCars.setLayoutManager(new LinearLayoutManager(requireContext()));

        extrasBinding.bookedCarsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBookedCarsActivity();
            }
        });
        extrasBinding.uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadCar();
            }
        });
        extrasBinding.locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isExpanded = extrasBinding.changeAccLt.getVisibility() == View.VISIBLE;
                toggleExpansion(extrasBinding.changeAccLt, isExpanded);
            }
        });
        extrasBinding.hideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isExpanded = extrasBinding.changeAccLt.getVisibility() == View.VISIBLE;
                toggleExpansion(extrasBinding.changeAccLt, isExpanded);
            }
        });

        if (bookedCars.size() == 0){
            showErrorLayoutNothing();
        }else {
            hideErrorLayout();
        }
        updateWelcomeText();

        if (getCurrentAccountType() != null){
            if (getCurrentAccountType().matches("Admin")){
                extrasBinding.adminAccessLt.setVisibility(View.GONE);
                extrasBinding.uploadLt.setVisibility(View.VISIBLE);
            }else {
                extrasBinding.adminAccessLt.setVisibility(View.VISIBLE);
                extrasBinding.uploadLt.setVisibility(View.GONE);
            }
        }else {
            extrasBinding.adminAccessLt.setVisibility(View.VISIBLE);
            extrasBinding.uploadLt.setVisibility(View.GONE);
        }

        handleAdminStatus();

        UploadedCarsHelper uploadedCarsHelper = new UploadedCarsHelper(requireContext());
        if (uploadedCarsHelper.getAllCars() != null){
            for (Car car: uploadedCarsHelper.getAllCars()) {
                if (car.getOwner_id().matches(getCurrentAccountId())) {
                    uploadedCars.add(car);
                }
            }
        }

        uploadedCarAdapter = new UploadedCarAdapter(requireContext(),uploadedCars);
        extrasBinding.uploadedCars.setAdapter(uploadedCarAdapter);
        extrasBinding.uploadedCars.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadHiredStatus(uploadedCars);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.account_types, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        extrasBinding.transactionTypesSpinner.setAdapter(new CustomSpinnerAdapter(requireContext(), R.layout.custom_spinner_dropdown_item,
                Arrays.asList(getResources().getTextArray(R.array.account_types))));


        extrasBinding.transactionTypesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                isDriverOptionSelected = position == 0; // Check if "Duration" option is selected
                // No need to show the time picker automatically when the Spinner selection changes
                // Instead, the time picker will be shown only when the etTimeAvailable EditText is clicked.

                if (isDriverOptionSelected){
                    extrasBinding.proceedBt.setText("Proceed for Driver");
                }else {
                    extrasBinding.proceedBt.setText("Proceed for Admin");
                }

                extrasBinding.proceedBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isDriverOptionSelected){
                            displayPasswordDialog(LoginActions.DRIVER_ACCESS);
                        }else {
                            displayPasswordDialog(LoginActions.ADMIN_ACCESS);
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        if (NewAppManager.getNewApp(requireContext())) {
            showFirstTimePrompt();
        }

        return extrasBinding.getRoot();
    }
    private void loadHiredStatus(List<Car> carList){
        if (carList != null) {
            List<String> carIds = new ArrayList<>();
            for (Car car : carList) {
                carIds.add(car.getCar_id());
            }
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        List<Hire> hires = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Hire hire = snapshot.getValue(Hire.class);
                            if (hire != null && hire.getOwner_id().equals(getCurrentAccountId())) {
                                // Notify listener about the updated location
                                hires.add(hire);
                            }
                        }
                        List<String> hiredIds = new ArrayList<>();
                        for (Hire hire: hires){
                            if (carIds.contains(hire.getCarId())){
                                hiredIds.add(hire.getCarId());
                            }
                        }
                        uploadedCarAdapter.setItems(hiredIds);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors if any
                }
            });
        }
    }
    private void showFirstTimePrompt() {
        TapTargetView.showFor((Activity) requireContext(),                 // Context
                TapTarget.forView(extrasBinding.locationButton,
                                "Account preferences", "Click here to change your account type")
                        .cancelable(true)
                        .transparentTarget(true)
                        .targetRadius(60),
                new TapTargetView.Listener() {
                    // Listener for actions after the tooltip is dismissed
                    @Override
                    public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                        // Add any further actions here if needed
                    }
                });
    }

    private void handleAdminStatus() {
        String descriptionText = getString(R.string.admin_access_description);

        // Find the index of the clickable text "[CHANGE PREFERENCES]"
        int changePreferencesStartIndex = descriptionText.indexOf("[GET ADMIN ACCESS]");

        // Only proceed if the clickable text is found in the original string
        if (changePreferencesStartIndex != -1) {
            SpannableString spannableString = new SpannableString(descriptionText);

            // Create a ClickableSpan for the clickable text
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    // Perform your action here, e.g., open the recommended activity
                    displayPasswordDialog(LoginActions.ADMIN_ACCESS);
                }
            };

            // Set the ClickableSpan to the part of the text that needs to be clickable
            spannableString.setSpan(clickableSpan, changePreferencesStartIndex, changePreferencesStartIndex + "[GET ADMIN ACCESS]".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Set the modified SpannableString to the TextView
            extrasBinding.accessText.setText(spannableString);
            extrasBinding.accessText.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            // If the clickable text is not found, simply set the original text to the TextView
            extrasBinding.accessText.setText(descriptionText);
        }
    }

    private void showMaps() {
        Intent intent = new Intent(requireContext(), MapsActivity.class);
        startActivity(intent);
    }

    private void updateWelcomeText() {
        if (getCurrentAccountUserName() != null){
            extrasBinding.welcomeText.setText(MessageFormat.format("Hi {0}", getCurrentAccountUserName()));
        }
    }

    private void showErrorLayout() {
        extrasBinding.errorLayout.setVisibility(View.VISIBLE);
    }
    private void hideErrorLayout() {
        extrasBinding.errorLayout.setVisibility(View.GONE);
    }
    private void showErrorLayoutNothing() {
        extrasBinding.errorLayout.setVisibility(View.VISIBLE);
        extrasBinding.errorText.setText("Nothing to show");
    }

    private void uploadCar() {
        Intent intent = new Intent(requireContext(), AddCarActivity.class);
        startActivity(intent);
    }

    private void openBookedCarsActivity() {
        Intent intent = new Intent(requireContext(), BookedActivity.class);
        startActivity(intent);
    }
    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
    public String getCurrentPassword() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserPassword", null);
    }
    public String getCurrentEmail() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserEmail", null);
    }
    public String getCurrentAccountUserName() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserName", null);
    }
    public String getCurrentAccountType() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentAccountType", null);
    }
    private void displayPasswordDialog(LoginActions loginActions) {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        passwordInputBinding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.password_input,
                null,false);
        dialog.setContentView(passwordInputBinding.getRoot());

        passwordInputBinding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordInputBinding.passwordEdittext.getText().toString();
                if (getCurrentEmail() != null) {
                    if (password.length() != 0) {
                        if (password.matches(getCurrentPassword())) {
                            extrasBinding.adminAccessContainer.setVisibility(View.GONE);
                            ProfileFetchRunnable profileFetchRunnable = new ProfileFetchRunnable(
                                    getCurrentEmail()
                                    , password, requireContext(),
                                    null, loginActions, null);

                            Thread thread = new Thread(profileFetchRunnable);
                            thread.start();
                            dialog.dismiss();
                        } else {
                            passwordInputBinding.passwordEdittext.setError("Wrong password!!");
                        }
                    } else {
                        passwordInputBinding.passwordEdittext.setError("Cannot be empty.");
                    }
                }else {
                    showSnackbar(extrasBinding.getRoot(),"You must have an account.");
                }
            }
        });

//        Product product = new Product(name,price,category,"100",);


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().getAttributes().windowAnimations = R.style.AdminNewProductAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER);

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
    private void toggleExpansion(View expandLayout, boolean isExpanded) {
        if (isExpanded) {
            expandLayout.setVisibility(View.GONE);
            ObjectAnimator.ofFloat(expandLayout, "scaleY", 1f, 0f)
                    .setDuration(300)
                    .start();
        } else {
            expandLayout.setVisibility(View.VISIBLE);
            ObjectAnimator.ofFloat(expandLayout, "scaleY", 0f, 1f)
                    .setDuration(300)
                    .start();
        }
    }
}