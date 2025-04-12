package com.sanny_tech.carapp.guides;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.google.android.material.snackbar.Snackbar;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.databinding.ActivityNewDriverGuideBinding;
import com.sanny_tech.carapp.databinding.PasswordInputBinding;
import com.sanny_tech.carapp.enums.LoginActions;

import java.util.concurrent.Executor;

public class NewDriverGuideActivity extends AppCompatActivity {
    private ActivityNewDriverGuideBinding binding;
    private long pageCount = 0;
    private PasswordInputBinding passwordInputBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_new_driver_guide);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageCount++;
                handlePage();
            }
        });
        binding.previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pageCount != 0) {
                    pageCount--;
                    handlePage();
                }else {
                    finish();
                }
            }
        });
        binding.skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBiometricPrompt();
            }
        });
    }

    private void handlePage() {
        if (pageCount == 1){
            binding.imagePlaceholder.setImageResource(R.drawable.add_a_service);
            binding.title1.setText("Tell us about your car");
            binding.title2.setText("Inform us about your car so that we can be able to improve your appearances on searches");
        }else if (pageCount == 2){
            binding.imagePlaceholder.setImageResource(R.drawable.tell_us_about_yourself);
            binding.title1.setText("Tell us about Yourself");
            binding.title2.setText("This helps us to verify that your identity as a certified driver with the proper licences.");
        } else if (pageCount == 3) {
            binding.imagePlaceholder.setImageResource(R.drawable.driver_payments);
            binding.title1.setText("We are a Subscription based company");
            binding.title2.setText("Earn Maximally from rides without commission deductions from each ride");
        }else if (pageCount == 4){
            showBiometricPrompt();
        }
    }
    private void showBiometricPrompt() {
        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG |
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL);

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            Executor executor = ContextCompat.getMainExecutor(this);
            BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                    new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS ||
                                    errorCode == BiometricPrompt.ERROR_HW_UNAVAILABLE) {
                                // Show password dialog if no biometrics are enrolled or hardware is unavailable
                                showPasswordDialog();
                            } else {
                                showSnackbar(binding.getRoot(),
                                        "Authentication error: " + errString);
                            }
                        }

                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            // Authentication succeeded, proceed with your action
                            Intent intent = new Intent(NewDriverGuideActivity.this, IdentitiesUploadActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            showSnackbar(binding.getRoot(),
                                    "Authentication failed");
                        }
                    });

            BiometricPrompt.PromptInfo.Builder promptInfoBuilder = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric login for Abiri Africa")
                    .setSubtitle("Get access using your biometric credential");

            // Check API level to set allowed authenticators
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API level 30 and above
                promptInfoBuilder.setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
            } else { // Below API level 30
                promptInfoBuilder.setDeviceCredentialAllowed(true);
            }

            BiometricPrompt.PromptInfo promptInfo = promptInfoBuilder.build();
            biometricPrompt.authenticate(promptInfo);
        } else {
            // No biometrics or device credentials are set up, show the password dialog
            showPasswordDialog();
        }
    }

    private void showPasswordDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        passwordInputBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.password_input,
                null, false);
        dialog.setContentView(passwordInputBinding.getRoot());

        passwordInputBinding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordInputBinding.passwordEdittext.getText().toString();
                if (getCurrentPassword() != null) {
                    if (password.length() != 0) {
                        if (password.matches(getCurrentPassword())) {
                            Intent intent = new Intent(NewDriverGuideActivity.this, IdentitiesUploadActivity.class);
                            startActivity(intent);
                            finish();
                            dialog.dismiss();
                        } else {
                            passwordInputBinding.passwordEdittext.setError("Wrong password!!");
                        }
                    } else {
                        passwordInputBinding.passwordEdittext.setError("Cannot be empty.");
                    }
                } else {
                    showSnackbar(binding.getRoot(), "You must have an account.");
                }
            }
        });

        dialog.show();
    }
    public String getCurrentPassword() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserPassword", null);
    }
    private void showSnackbar(View rootView, String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.black));
        snackbar.setTextColor(ContextCompat.getColor(this,R.color.white));
        snackbar.show();
    }
}