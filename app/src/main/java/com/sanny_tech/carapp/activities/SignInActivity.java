package com.sanny_tech.carapp.activities;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.credentials.CredentialManager;
import androidx.databinding.DataBindingUtil;
import androidx.loader.content.Loader;


import com.example.clientlib.NimbusPushService;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.asynctasks.ProfileFetchRunnable;
import com.sanny_tech.carapp.asynctasks.UserLoader;
import com.sanny_tech.carapp.databasehelpers.DatabaseHelper;
import com.sanny_tech.carapp.databinding.ActivitySignInBinding;
import com.sanny_tech.carapp.dialogs.ProgressFragment;
import com.sanny_tech.carapp.entities.User;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sanny_tech.carapp.entities.UserDTO;
import com.sanny_tech.carapp.enums.ActionType;
import com.sanny_tech.carapp.enums.LoginActions;
import com.sanny_tech.carapp.utils.FCMTokenManager;
import com.sanny_tech.carapp.utils.SimCardManager;

import java.util.Date;
import java.util.Objects;

public class SignInActivity extends AppCompatActivity implements
        ProfileFetchRunnable.OnFinishLoadListener {
    private ActivitySignInBinding signInBinding;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "SignActivity";

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private boolean showOneTapUI = true;
    private String clientId;
    private FirebaseUser currentUser;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private ProgressFragment progressFragment;
    private User profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signInBinding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in);
        setSupportActionBar(signInBinding.signInToolbar);
        signInBinding.signInToolbar.setNavigationOnClickListener(v -> onBackPressed());

        CredentialManager credentialManager = CredentialManager.create(this);
        fetchWebClientId();
        mAuth = FirebaseAuth.getInstance();
        oneTapClient = Identity.getSignInClient(this);

        // Configure One Tap sign-in request
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId(getString(R.string.default_web_client_id)) // Replace with your web client ID
                                .setFilterByAuthorizedAccounts(false)
                                .build())
                .build();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Already have an account");
        }

        signInBinding.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Objects.requireNonNull(signInBinding.signInEmailTextField.getText()).toString();
                String password = Objects.requireNonNull(signInBinding.signInPasswordTextField.getText()).toString();

                if (email.length() == 0 || password.length() == 0) {
                    Toast.makeText(SignInActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                } else {
                    loginToServer(convertAmount(email), password,"sign");
                }
            }
        });
        signInBinding.createProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SimCardManager.getPhoneNumber(SignInActivity.this) != null &&
                !SimCardManager.getPhoneNumber(SignInActivity.this).equals("")){
                    openCreateAccount();
                }else {
                    openCreateAccountActivity();
                }
            }
        });
        signInBinding.continueWithEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isExpanded = signInBinding.passwordSkele.getVisibility() == View.VISIBLE;
                toggleExpansion(signInBinding.passwordSkele,isExpanded);
            }
        });
        signInBinding.googleSigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showOneTapUI) {
                    googleSignIn();
                }else {
                    loginToServer(currentUser.getEmail(),"Google","gsign");
                }
            }
        });
    }
    private String convertAmount(String amount) {
        // Remove commas from the input amount
        return amount.replace(" ", "");
    }
    private void loginToServer(String email, String password,String button) {
        if (button.equals("sign")) {
            signIn(email,password);
        } else if (button.equals("gsign")) {
            if (currentUser != null){
                signIn(currentUser.getEmail(),"Google");
            }
        }
    }

    private void signIn(String email, String password) {
        if (isNetworkConnected()) {
            signInBinding.signInButton.setVisibility(View.GONE);
            ProfileFetchRunnable profileFetchRunnable = new ProfileFetchRunnable(
                    email, password, SignInActivity.this,
                    signInBinding.progressBar, LoginActions.LOGIN, null, "");

            Thread thread = new Thread(profileFetchRunnable);
            thread.start();
            profileFetchRunnable.setOnFinishLoadListener(SignInActivity.this);
        } else {
            Toast.makeText(SignInActivity.this, "You're offline", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchWebClientId() {
        DatabaseReference hireListener = FirebaseDatabase.getInstance().getReference("configurations");
        hireListener.addValueEventListener(new ValueEventListener() {
            // Inside onDataChange method of ValueEventListener
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String key = snapshot.getKey();
                        if ("client_id".equals(key)) {
                            clientId = snapshot.getValue(String.class);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void showOptionsMenu(View view) {
        Context wrapper = new ContextThemeWrapper(this, R.style.PopupMenuStyle);
        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        popupMenu.getMenuInflater().inflate(R.menu.signin_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.new_account) {
                openCreateAccount();
                return true;
            } else if (itemId == R.id.feed_back) {
                showFeedbackBottomSheet();
                return true;
            } else if (itemId == R.id.back) {
                onBackPressed();
                return true;
            } else {
                return false;
            }
        });
        popupMenu.show();
    }
    private void toggleExpansion(View expandLayout, boolean isExpanded) {
        if (isExpanded) {
            expandLayout.setVisibility(View.GONE);
            ObjectAnimator.ofFloat(expandLayout, "scaleY", 1f, 0f)
                    .setDuration(300)
                    .start();
            signInBinding.googleSigin.setVisibility(View.VISIBLE);
        } else {
            expandLayout.setVisibility(View.VISIBLE);
            ObjectAnimator.ofFloat(expandLayout, "scaleY", 0f, 1f)
                    .setDuration(300)
                    .start();
            signInBinding.googleSigin.setVisibility(View.GONE);
        }
    }
    private void openCreateAccount() {
        Intent intent = new Intent(SignInActivity.this, CreateAccountActivity.class)
                .setAction("create account");
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void showFeedbackBottomSheet() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_feedback, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Find views in the bottom sheet layout
        RatingBar ratingBar = bottomSheetView.findViewById(R.id.ratingBar);
        TextInputLayout feedbackTextInputLayout = bottomSheetView.findViewById(R.id.feedbackTextInputLayout);
        TextInputEditText feedbackEditText = bottomSheetView.findViewById(R.id.feedbackEditText);
        MaterialButton submitButton = bottomSheetView.findViewById(R.id.submitButton);

        submitButton.setOnClickListener(v -> {
            // Get feedback and rating inputs
            String feedback = feedbackEditText.getText().toString();
            float rating = ratingBar.getRating();

            // Perform your submission logic here
            // You can send the feedback and rating to a server or store them locally
            // Then dismiss the bottom sheet
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    @Override
    public void onResponse(String response) {
        if (response != null) {
            signInBinding.signInButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            showOneTapUI = false;
        }
    }

    private void googleSignIn() {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, result -> {
                    try {
                        startIntentSenderForResult(result.getPendingIntent().getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
                    } catch (Exception e) {
                        Log.e(TAG, "Error starting sign-in intent", e);
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Error initiating sign-in", e);
                    Toast.makeText(SignInActivity.this, "Sign-in failed", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                SignInCredential googleCredential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = googleCredential.getGoogleIdToken();
                if (idToken != null) {
                    AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                    mAuth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "signInWithCredential:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        firebaseAuthWithGoogle(idToken);
                                    } else {
                                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                                    }
                                }
                            });
                } else {
                    Log.w(TAG, "No ID token returned");

                }
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);

            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                checkUserExistence(user);
                            }else {
                                Toast.makeText(SignInActivity.this,
                                        "Sign in failed", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void checkUserExistence(FirebaseUser user) {
        UserDTO userRequest = new UserDTO(user.getEmail(), "Google",
                user.getDisplayName(),user.getPhoneNumber(), FCMTokenManager.getToken(this));
        UserLoader userLoader = new UserLoader(this, user.getEmail(), "Google",
                FCMTokenManager.getToken(this), ActionType.GET_USER, "",
                userRequest);
        showProgressBar();
        userLoader.forceLoad();
        userLoader.registerListener(7, new Loader.OnLoadCompleteListener<String>() {
            @Override
            public void onLoadComplete(@androidx.annotation.NonNull Loader<String> loader, @androidx.annotation.Nullable String data) {
                hideProgreeBar();
                if (data != null) {
                    Toast.makeText(SignInActivity.this, "Setting up your account", Toast.LENGTH_SHORT).show();
                    setWallPaper(String.valueOf(user.getPhotoUrl()));
                    if (user.getPhoneNumber() != null){
                    SimCardManager.setPhoneNumber(SignInActivity.this,
                            user.getPhoneNumber());
                    }
                    setUserName(user.getDisplayName());
                    loginToServer(user.getEmail(), "Google", "sign");
                } else {
                    Toast.makeText(SignInActivity.this, "Something went wrong",
                            Toast.LENGTH_SHORT).show();
                    showDeleteConfirmationDialog(user);
                }
            }
        });
    }

    private void setUserName(String displayName) {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("currentUserName", displayName);
        editor.apply();
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
    private void openCreateAccountActivity() {
        Intent intent = new Intent(SignInActivity.this, AddPhoneNumberActivity.class);
        intent.putExtra("instruction","sign-up");
        startActivity(intent);
    }
    public String getCurrentAccountEmail() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentEmail", null);
    }
    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
    private void showDeleteConfirmationDialog(FirebaseUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Account")
                .setMessage("Create account as " + user.getDisplayName())
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        profile = new User();
                        profile.setUsername(user.getDisplayName());
                        if (user.getPhotoUrl() != null) {
                            profile.setProfilePic(user.getPhotoUrl().toString());
                        }
                        profile.setUsername(user.getDisplayName());
                        profile.setDateCreated(String.valueOf(System.currentTimeMillis()));
                        profile.setUserId(user.getUid());
                        profile.setEmail(user.getEmail());
                        profile.setPassword("Google");
                        profile.setAccountType("user");
                        profile.setDateCreated(String.valueOf(new Date()));
                        if (user.getPhoneNumber() != null && !user.getPhoneNumber().equals("")) {
                            SimCardManager.setPhoneNumber(SignInActivity.this,user.getPhoneNumber());
                            createUser(profile);
                        }else {
                            Toast.makeText(SignInActivity.this, "Phone number required", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignInActivity.this, AddPhoneNumberActivity.class);
                            intent.putExtra("instruction","gsign-up");
                            intent.putExtra("user",profile);
                            startActivity(intent);
                            finish();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void showProgressBar() {
        progressFragment = new ProgressFragment();
        progressFragment.show(getSupportFragmentManager(), "progress_dialog");
    }

    private void hideProgreeBar() {
        progressFragment.dismiss();
    }
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
                    Toast.makeText(SignInActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                    saveUser(user);
                    ProfileFetchRunnable profileFetchRunnable = new ProfileFetchRunnable(user.getEmail(),
                            user.getPassword(), SignInActivity.this,
                            null, LoginActions.LOGIN, null, user.getUsername());

                    Thread thread = new Thread(profileFetchRunnable);
                    thread.start();
                } else {
                    Toast.makeText(SignInActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void setCurrentProfile(User selectedProfile) {
        SharedPreferences sharedPreferences = SignInActivity.this.getSharedPreferences("AccountPrefs", MODE_PRIVATE);
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

}