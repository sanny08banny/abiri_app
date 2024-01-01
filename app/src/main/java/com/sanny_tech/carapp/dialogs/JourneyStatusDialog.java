package com.sanny_tech.carapp.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.utils.JourneyStatusManager;

import java.text.MessageFormat;

public class JourneyStatusDialog {

    private AlertDialog dialog;
    private EditText chargesEditText;
    private Button startStopButton;
    private TextView destinationText;

    private JourneyStatusManager journeyStatusManager;
    private JourneyStatusListener journeyListener;

    public interface JourneyStatusListener {
        void onJourneyComplete(boolean isSuccess, String chargesText);
    }

    public void setBookingListener(JourneyStatusListener listener) {
        this.journeyListener = listener;
    }
    public JourneyStatusDialog(Context context,String destination) {
        journeyStatusManager = new JourneyStatusManager(context);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_journey_status, null);
        builder.setView(dialogView);

        destinationText = dialogView.findViewById(R.id.destination_edit_text);
        chargesEditText = dialogView.findViewById(R.id.charges_edit_text);
        startStopButton = dialogView.findViewById(R.id.start_stop_button);

        destinationText.setText(MessageFormat.format("Journey destination is {0}", destination));
        destinationText.setEnabled(false); // Disable editing the destination initially

        dialog = builder.create();

        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleStartStopButtonClick();
            }
        });
        dialog.setCancelable(false);

        updateStartStopButton();
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    private void handleStartStopButtonClick() {
        boolean isJourneyStarted = journeyStatusManager.isJourneyStarted();
        if (isJourneyStarted) {
            String chargesText = chargesEditText.getText().toString().trim();
            if (!chargesText.matches("")) {
                journeyStatusManager.setJourneyStarted(false);
                journeyListener.onJourneyComplete(true, chargesText);
                dismiss();
            }else {
                Toast.makeText(dialog.getContext(),
                        "Enter the charges for the journey before stopping", Toast.LENGTH_SHORT).show();
                chargesEditText.setError("Enter the charges for the journey before stopping");
            }
            // Perform actions for stopping the journey
        } else {
            // Read destination coordinates if needed
            // Perform actions for starting the journey, e.g., parse destinationText and save it

            journeyStatusManager.setJourneyStarted(true);
            chargesEditText.setVisibility(View.VISIBLE);
            chargesEditText.setError("Enter the charges for the journey before stopping");
            // Perform actions for starting the journey
        }

        updateStartStopButton();
    }

    private void updateStartStopButton() {
        boolean isJourneyStarted = journeyStatusManager.isJourneyStarted();
        if (isJourneyStarted) {
            startStopButton.setText("Stop Journey");
        } else {
            startStopButton.setText("Start Journey");
        }
    }
}
