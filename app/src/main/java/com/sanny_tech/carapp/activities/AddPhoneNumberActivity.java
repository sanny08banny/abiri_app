package com.sanny_tech.carapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.loader.content.Loader;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.asynctasks.ProfileFetchRunnable;
import com.sanny_tech.carapp.asynctasks.UserLoader;
import com.sanny_tech.carapp.databasehelpers.DatabaseHelper;
import com.sanny_tech.carapp.databinding.ActivityAddPhoneNumberBinding;
import com.sanny_tech.carapp.dialogs.ProgressFragment;
import com.sanny_tech.carapp.entities.User;
import com.sanny_tech.carapp.enums.ActionType;
import com.sanny_tech.carapp.enums.LoginActions;
import com.sanny_tech.carapp.utils.FCMTokenManager;
import com.sanny_tech.carapp.utils.SimCardManager;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.TimeUnit;

public class AddPhoneNumberActivity extends AppCompatActivity {

    private ActivityAddPhoneNumberBinding addPhoneNumberBinding;
    private SimCardManager simCardManager;
    private String phoneNumber;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String formattedPhoneNumber;
    private String instruction;
    private User user;
    private ProgressFragment progressFragment;
    private DatabaseReference logsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPhoneNumberBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_phone_number);
        mAuth = FirebaseAuth.getInstance();
        logsReference = FirebaseDatabase.getInstance().getReference("phone_logs");
        instruction = getIntent().getStringExtra("instruction");
        user = getIntent().getParcelableExtra("user");
        if (instruction.equals("verification") || instruction.equals("gsign-up")) {
            addPhoneNumberBinding.googleSigin.setVisibility(View.GONE);
        }
        addPhoneNumberBinding.googleSigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddPhoneNumberActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });
        addPhoneNumberBinding.continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPhoneNumber = addPhoneNumberBinding.addPhoneNumberEdittext.getText().toString().trim();

// Validate the input format (7xxxxxxxx)
                if (newPhoneNumber.matches("7\\d{8}") || newPhoneNumber.matches("1\\d{8}")) {
                    // Format the phone number to match the desired format (254input)
                    formattedPhoneNumber = "+254" + newPhoneNumber;
//                    sendVerificationCode(formattedPhoneNumber);
//                    addPhoneNumberBinding.mainLt.setVisibility(View.GONE);
//                    addPhoneNumberBinding.verifyLt.setVisibility(View.VISIBLE);
                    completeSetup();
                } else if (newPhoneNumber.matches("01\\d{8}")) {
                    // Format the phone number to match the desired format (254input)
                    formattedPhoneNumber = convertAmount(newPhoneNumber);
                    sendVerificationCode(formattedPhoneNumber);
                    addPhoneNumberBinding.mainLt.setVisibility(View.GONE);
                    addPhoneNumberBinding.verifyLt.setVisibility(View.VISIBLE);
                } else {
                    addPhoneNumberBinding.addPhoneNumberEdittext.setError("Invalid format. Please use 7xxxxxxxx format.");
                }
            }
        });
        addPhoneNumberBinding.verifyButton.setOnClickListener(v -> {
            String code = addPhoneNumberBinding.otpEditText.getText().toString();
            if (!code.isEmpty()) {
                SimCardManager.setPhoneNumber(AddPhoneNumberActivity.this, formattedPhoneNumber);
                verifyCode(code);
            } else {
                addPhoneNumberBinding.otpEditText.setError("OTP code is required");
            }
        });
    }

    // In your activity or fragment
    private void showSnackbar(View rootView, String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.blue));
        snackbar.show();
    }

    private String convertAmount(String amount) {
        // Remove commas from the input amount
        return amount.replace(" ", "");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        completeSetup();
    }

    private void completeSetup() {
        SimCardManager.setPhoneNumber(AddPhoneNumberActivity.this, formattedPhoneNumber);
        if (instruction.equals("verification")) {
            if (!SimCardManager.getPhoneNumber(AddPhoneNumberActivity.this).equals("")) {
                showSnackbar(addPhoneNumberBinding.getRoot(), formattedPhoneNumber + " added successfully");
                Intent intent = new Intent();
                intent.putExtra("selectedNo", formattedPhoneNumber);
                setResult(RESULT_OK, intent);
                finish();
            }
        } else if (instruction.equals("gsign-up")) {
            createUser(user);
        } else {
            if (!SimCardManager.getPhoneNumber(AddPhoneNumberActivity.this).equals("")) {
                showSnackbar(addPhoneNumberBinding.getRoot(), formattedPhoneNumber + " added successfully");
                Intent intent = new Intent(AddPhoneNumberActivity.this, CreateAccountActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    // Add the callback instance here
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    logsReference.child(getCurrentAccountEmail()).setValue("success");
                    completeSetup();
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Log.w("OTP", "Verification failed", e);
                    logsReference.child(getCurrentAccountEmail()).setValue("Verification failed" +  e);
                }

                @Override
                public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    logsReference.child(getCurrentAccountEmail()).setValue("code sent");
                    mVerificationId = verificationId;
                    mResendToken = token;
                }
                @Override
                public void onCodeAutoRetrievalTimeOut(String verificationId) {
                    logsReference.child(getCurrentAccountEmail()).setValue("Code auto retrieval timeout: " + verificationId);
                    Log.d("PhoneAuth", "Code auto retrieval timeout: " + verificationId);
                    // Handle timeout
                }
            };

    private void createUser(User user) {
        UserLoader userLoader = new UserLoader(this, user.getEmail(), user.getPassword(),
                FCMTokenManager.getToken(this), ActionType.BOOK, user.getUsername(),
                null);
        showProgressBar();
        userLoader.forceLoad();
        userLoader.registerListener(7, new Loader.OnLoadCompleteListener<String>() {
            @Override
            public void onLoadComplete(@androidx.annotation.NonNull Loader<String> loader, @androidx.annotation.Nullable String data) {
                hideProgreeBar();
                if (data != null) {
                    Toast.makeText(AddPhoneNumberActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                    saveUser(user);
                    ProfileFetchRunnable profileFetchRunnable = new ProfileFetchRunnable(user.getEmail(),
                            user.getPassword(), AddPhoneNumberActivity.this,
                            null, LoginActions.LOGIN, null, user.getUsername());

                    Thread thread = new Thread(profileFetchRunnable);
                    thread.start();
                } else {
                    Toast.makeText(AddPhoneNumberActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setCurrentProfile(User selectedProfile) {
        SharedPreferences sharedPreferences = AddPhoneNumberActivity.this.getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("currentUserId", selectedProfile.getUserId());
        editor.putString("currentAccountType", selectedProfile.getAccountType());
        editor.putString("currentUserEmail", selectedProfile.getEmail());
        editor.putString("currentUserName", selectedProfile.getUsername());
        editor.putString("currentDateJoined", selectedProfile.getDateCreated());
        editor.putString("currentUserPassword", selectedProfile.getPassword());
        editor.putString("currentProfileImage", selectedProfile.getProfilePic());
        editor.apply();
    }

    private void saveUser(User profile) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        User existingUser = databaseHelper.getUserById(profile.getUserId());
        if (existingUser == null) {
            String accountSavedLocally = databaseHelper.addUser(profile);
            if (accountSavedLocally.length() != 0) {
                Toast.makeText(this, "Account saved successfully", Toast.LENGTH_SHORT).show();
                setCurrentProfile(profile);
                setWallPaper(profile.getProfilePic());
            }
        } else {
            Toast.makeText(this, "This user already exists.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgressBar() {
        progressFragment = new ProgressFragment();
        progressFragment.show(getSupportFragmentManager(), "progress_dialog");
    }

    private void hideProgreeBar() {
        progressFragment.dismiss();
    }

    private void setWallPaper(String selectedImagePath) {
        // Get a reference to SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        if (getCurrentAccountEmail() != null) {
            DatabaseHelper databaseHelper = new DatabaseHelper(this);
            User currentUser = databaseHelper.getUserById(getCurrentAccountId());
            currentUser.setProfilePic(selectedImagePath);
            databaseHelper.updateUser(currentUser);
        }

// Save the chatWallpaper string
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("profilePic", selectedImagePath);
        editor.apply();

        String savedChatWallpaper = sharedPreferences.getString("profilePic", "default_dp_path");

    }

    public String getCurrentAccountEmail() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentEmail", null);
    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
}