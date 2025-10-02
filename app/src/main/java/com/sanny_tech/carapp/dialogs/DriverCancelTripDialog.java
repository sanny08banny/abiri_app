package com.sanny_tech.carapp.dialogs;// DriverCancelTripDialog.java
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioButton;

import com.sanny_tech.carapp.R;

public class DriverCancelTripDialog extends Dialog {

    private RadioGroup radioGroup;
    private EditText customReasonEditText;
    private Button cancelButton;
    private CancelTripListener listener;

    public DriverCancelTripDialog(Context context, CancelTripListener listener) {
        super(context);
        this.listener = listener;
        setContentView(R.layout.dialog_driver_cancel_trip);
        radioGroup = findViewById(R.id.rg_driver_cancel_reasons);
        customReasonEditText = findViewById(R.id.et_driver_custom_reason);
        cancelButton = findViewById(R.id.btn_driver_cancel_trip);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedReason = getSelectedReason();
                listener.onCancelTrip(selectedReason);
                dismiss();
            }
        });
    }

    private String getSelectedReason() {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        String reason = "";

        if (selectedId == R.id.rb_driver_reason_1) {
            reason = "Passenger is late";
        } else if (selectedId == R.id.rb_driver_reason_2) {
            reason = "Passenger did not show up";
        } else if (selectedId == R.id.rb_driver_reason_3) {
            reason = "Emergency";
        } else {
            reason = customReasonEditText.getText().toString();
        }

        return reason;
    }

    public interface CancelTripListener {
        void onCancelTrip(String reason);
    }
}
