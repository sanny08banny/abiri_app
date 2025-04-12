package com.sanny_tech.carapp.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.BookedActivity;
import com.sanny_tech.carapp.activities.ManageProfiles;
import com.sanny_tech.carapp.activities.ProfileActivity;
import com.sanny_tech.carapp.activities.SignInActivity;
import com.sanny_tech.carapp.adapters.HorizontalItemAdapter;
import com.sanny_tech.carapp.adapters.PermissionAdapter;
import com.sanny_tech.carapp.adapters.UploadedCarAdapter;
import com.sanny_tech.carapp.asynctasks.ProfileFetchRunnable;
import com.sanny_tech.carapp.asynctasks.TokensLoader;
import com.sanny_tech.carapp.databinding.DialogAdminLtBinding;
import com.sanny_tech.carapp.databinding.FragmentExtrasBinding;
import com.sanny_tech.carapp.databinding.PasswordInputBinding;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.entities.OptionItem;
import com.sanny_tech.carapp.entities.Permission;
import com.sanny_tech.carapp.enums.LoginActions;
import com.sanny_tech.carapp.enums.TokenAction;
import com.sanny_tech.carapp.guides.NewDriverGuideActivity;
import com.sanny_tech.carapp.hire_utils.HireActivity;
import com.sanny_tech.carapp.taxi_utils.TaxiInit;
import com.sanny_tech.carapp.utils.AdminManager;
import com.sanny_tech.carapp.utils.NewAppManager;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sanny_tech.carapp.utils.TaxiModeManager;
import com.sanny_tech.carapp.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExtrasFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExtrasFragment extends Fragment implements AdminManager.AdminStatusCallback,
        LoaderManager.LoaderCallbacks<String>, HorizontalItemAdapter.OnItemClickListener {
    private FragmentExtrasBinding extrasBinding;
    private List<Car> uploadedCars = new ArrayList<>();
    private UploadedCarAdapter uploadedCarAdapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private PasswordInputBinding passwordInputBinding;
    private boolean isDriverOptionSelected;
    private DatabaseReference reference;
    private boolean isMainAdmin = false;
    private AdminManager adminManager;
    private int clicked = 0;
    private TokenManager tokenManager;
    private List<OptionItem> items = new ArrayList<>();
    private HorizontalItemAdapter horizontalItemAdapter;
    private String profileImage;
    private static final int REQUEST_CALL_PERMISSION = 1;
    private static final String EMERGENCY_PHONE_NUMBER = "0721220054"; // Replace with the appropriate emergency number
    private static final String EMERGENCY_EMAIL = "support@abiriapp.com"; // Replace with the appropriate emergency email
    private static final String EMERGENCY_WHATSAPP_NUMBER = "+254721220054"; // Replace with the appropriate WhatsApp number


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
        extrasBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_extras, container, false);
        tokenManager = new TokenManager(requireContext());
        reference = FirebaseDatabase.getInstance().getReference("hires"); // Replace with your Firebase location reference
        adminManager = new AdminManager();
        adminManager.getAdminAccess(getCurrentAccountId(), this);
        OptionItem optionItem = new OptionItem("Settings", "", "");
        OptionItem optionItem1 = new OptionItem("Become a service provider and earn",
                "", "");
        if (items.size() == 0) {
            items.add(optionItem);
            items.add(optionItem1);
        }
        horizontalItemAdapter = new HorizontalItemAdapter(requireContext(), items);
        horizontalItemAdapter.setOnItemClickListener(this);
        extrasBinding.optionsRec.setAdapter(horizontalItemAdapter);
        extrasBinding.optionsRec.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (getCurrentAccountId() != null && getCurrentAccountUserName() != null) {
            extrasBinding.userName.setText(getCurrentAccountUserName());
            extrasBinding.userEmail.setText(getCurrentEmail());
        }
        profileImage = getWallPaper();
        if (getCurrentAccountId() == null) {
        } else {
            updateTokens();
            if (getCurrentAccountId().isEmpty()) {
            } else {
                updateProfileImage(profileImage, extrasBinding.userImage);
            }
        }

        String descriptionText = getString(R.string.edit_profile);

        // Find the index of the clickable text "[CHANGE PREFERENCES]"
        int changePreferencesStartIndex = descriptionText.indexOf("Edit profile");

        // Only proceed if the clickable text is found in the original string
        if (changePreferencesStartIndex != -1) {
            SpannableString spannableString = new SpannableString(descriptionText);

            // Create a ClickableSpan for the clickable text
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    // Perform your action here, e.g., open the recommended activity
                    Intent intent = new Intent(requireContext(), ManageProfiles.class);
                    startActivity(intent);
                }
            };

            // Set the ClickableSpan to the part of the text that needs to be clickable
            spannableString.setSpan(clickableSpan, changePreferencesStartIndex,
                    changePreferencesStartIndex + "Edit profile".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Set the modified SpannableString to the TextView
            extrasBinding.editDate.setText(spannableString);
            extrasBinding.editDate.setMovementMethod(LinkMovementMethod.getInstance());
        }
        extrasBinding.buttonSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSOSOptions();
            }
        });
        return extrasBinding.getRoot();
    }

    private void showFirstTimePrompt() {
        TapTargetView.showFor((Activity) requireContext(),                 // Context
                TapTarget.forView(extrasBinding.getRoot(),
                                "Account preferences", "Click here to change your account type")
                        .cancelable(true)
                        .transparentTarget(false)
                        .targetRadius(60),
                new TapTargetView.Listener() {
                    // Listener for actions after the tooltip is dismissed
                    @Override
                    public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                        // Add any further actions here if needed
                    }
                });
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
        passwordInputBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.password_input,
                null, false);
        dialog.setContentView(passwordInputBinding.getRoot());

        passwordInputBinding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordInputBinding.passwordEdittext.getText().toString();
                if (getCurrentEmail() != null) {
                    if (password.length() != 0) {
                        if (password.matches(getCurrentPassword())) {
                            finishSetup(password, loginActions, dialog);
                        } else {
                            passwordInputBinding.passwordEdittext.setError("Wrong password!!");
                        }
                    } else {
                        passwordInputBinding.passwordEdittext.setError("Cannot be empty.");
                    }
                } else {
                    showSnackbar(extrasBinding.getRoot(), "You must have an account.");
                }
            }
        });

//        Product product = new Product(name,price,category,"100",);


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().getAttributes().windowAnimations = R.style.AdminNewProductAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER);

    }

    private void finishSetup(String password, LoginActions loginActions, Dialog dialog) {
        if (loginActions.equals(LoginActions.FUN_ADMIN_ACCESS)) {
            adminManager.requestAdminAccess(getCurrentAccountId());
            Toast.makeText(requireContext(),
                    "Successful, restart to load changes",
                    Toast.LENGTH_SHORT).show();
            if (dialog != null) {
                dialog.dismiss();
            }
        } else {
            ProfileFetchRunnable profileFetchRunnable = new ProfileFetchRunnable(
                    getCurrentEmail()
                    , password, requireContext(),
                    null, loginActions, null, getCurrentAccountUserName());

            Thread thread = new Thread(profileFetchRunnable);
            thread.start();
            if (dialog != null) {
                dialog.dismiss();
            }
        }
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

    private void showAdminDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        DialogAdminLtBinding dialogAdminLtBinding = DataBindingUtil.inflate(inflater,
                R.layout.dialog_admin_lt, null, false);
        View dialogView = dialogAdminLtBinding.getRoot();
        dialogBuilder.setView(dialogView);

        if (getCurrentAccountType() != null) {
            List<Permission> permissions = new ArrayList<>();
            if (TaxiModeManager.getTaxiMode(requireContext())) {
                permissions.add(new Permission("Taxi access", true));
            } else {
                permissions.add(new Permission("Taxi access", false));
            }
            if (getCurrentAccountType().matches("Admin")) {
                permissions.add(new Permission("Admin access", true));
            } else {
                permissions.add(new Permission("Admin access", false));
            }
            if (isMainAdmin) {
                permissions.add(new Permission("Fun admin access", true));
            } else {
                permissions.add(new Permission("Fun admin access", false));
            }
            PermissionAdapter permissionAdapter = new PermissionAdapter(requireContext(),
                    permissions, "");

            dialogAdminLtBinding.permissions.setAdapter(permissionAdapter);
            dialogAdminLtBinding.permissions.setLayoutManager(
                    new LinearLayoutManager(requireContext()));

            permissionAdapter.setOnItemClickListener(new PermissionAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Permission item) {
                    if (item.getName().equals("Taxi access")) {
//                        displayPasswordDialog(LoginActions.DRIVER_ACCESS);
                        Intent intent = new Intent(requireContext(), NewDriverGuideActivity.class);
                        startActivity(intent);
                    } else if (item.getName().equals("Admin access")) {
                        if (!getCurrentPassword().equals("google")) {
                            displayPasswordDialog(LoginActions.ADMIN_ACCESS);
                        } else {
                            finishSetup("google", LoginActions.ADMIN_ACCESS, null);
                        }
                    } else if (item.getName().equals("Fun admin access")) {
                        if (!getCurrentPassword().equals("google")) {
                            displayPasswordDialog(LoginActions.FUN_ADMIN_ACCESS);
                        } else {
                            finishSetup("google", LoginActions.FUN_ADMIN_ACCESS, null);
                        }
                    }
                }
            });
        } else {
        }

        dialogBuilder.setTitle("Admin backdrop");
//        dialogBuilder.setPositiveButton("About", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//
//            }
//        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing on cancel
                dialog.dismiss();
            }
        });
        AlertDialog b = dialogBuilder.create();

        // Apply the animation to the dialog
        Animation scaleAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_up_dialog);
        dialogView.startAnimation(scaleAnimation);

        b.show();
    }

    @Override
    public void onAdminStatusChecked(boolean isAdmin) {
        isMainAdmin = isAdmin;
    }

    public void getAllTaxiInitForUser(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("taxis")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            TaxiInit taxiInit = document.toObject(TaxiInit.class);
                            // Handle each TaxiInit object
                            Log.d("Firestore", "TaxiInit: " + taxiInit.toString());
                        }
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                    }
                });
    }

    private void updateTokens() {
        // Retrieve the token amount
        double storedTokenAmount = tokenManager.getTokenAmount();
        if (storedTokenAmount != 0.0) {
            extrasBinding.tokenBalance.setText(String.valueOf(storedTokenAmount));
        }
        LoaderManager.getInstance(this).initLoader(4, null, this);
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        return new TokensLoader(requireContext(), null, TokenAction.GET);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        if (data != null) {
            double tokens = Double.parseDouble(data);
            tokenManager.setTokenAmount(tokens);
            extrasBinding.tokenBalance.setText(data);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    @Override
    public void onItemClick(OptionItem item) {
        if (item.getTitle().equals("Settings")) {
            Intent intent = new Intent(requireContext(), ProfileActivity.class);
            startActivity(intent);
        } else if (item.getTitle().equals("Become a service provider and earn")) {
            if (NewAppManager.getNewApp(requireContext())) {
                showAdminDialog();
            } else {
                showAdminDialog();
            }
        }
    }

    private String getWallPaper() {
        // Get a reference to SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);

        return sharedPreferences.getString("profilePic", null);
    }

    private void updateProfileImage(String selectedImage, ImageView imageView) {
        if (selectedImage != null) {
            Glide.with(this)
                    .load(selectedImage)
                    .override(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT) // Set thedesired width and height for resizing
                    .into(imageView);
        }
    }

    private void showSOSOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Contact Emergency Services")
                .setItems(new CharSequence[]{"Call", "WhatsApp", "Email"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                makeEmergencyCall();
                                break;
                            case 1:
                                sendWhatsAppMessage();
                                break;
                            case 2:
                                sendEmergencyEmail();
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    private void makeEmergencyCall() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        } else {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + EMERGENCY_PHONE_NUMBER));
            startActivity(callIntent);
        }
    }

    private void sendWhatsAppMessage() {
        try {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, "SOS! I need help.");
            sendIntent.putExtra("jid", EMERGENCY_WHATSAPP_NUMBER + "@s.whatsapp.net"); // WhatsApp number with country code
            sendIntent.setPackage("com.whatsapp");
            startActivity(sendIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), "WhatsApp is not installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmergencyEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + EMERGENCY_EMAIL));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "SOS Emergency");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "SOS! I need help.");
        try {
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), "No email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeEmergencyCall();
            } else {
                Toast.makeText(requireContext(), "Permission DENIED to make emergency call", Toast.LENGTH_SHORT).show();
            }
        }
    }
}