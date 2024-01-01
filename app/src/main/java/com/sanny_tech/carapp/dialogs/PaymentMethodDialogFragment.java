package com.sanny_tech.carapp.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.PaymentAdapter;
import com.sanny_tech.carapp.entities.TaxiLocation;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodDialogFragment extends DialogFragment implements PaymentAdapter.OnItemClickListener{
    private PaymentAdapter adapter;
    private List<String> payments;
    private PaymentSelectedListener listener;
    private TaxiLocation taxiLocation;

    public PaymentMethodDialogFragment(TaxiLocation taxiLocation) {
        this.taxiLocation = taxiLocation;
    }
    public interface PaymentSelectedListener {
        void onSelectionResponse(String item);
    }

    public void setBookingListener(PaymentSelectedListener listener) {
        this.listener = listener;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.payment_method_dialog, null);

        RecyclerView paymentMethodsList = dialogView.findViewById(R.id.paymentMethodsList);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        payments = new ArrayList<>();
        payments.add("Cash");
        adapter = new PaymentAdapter(payments, requireContext());
        adapter.setOnItemClickListener(this);
        paymentMethodsList.setAdapter(adapter);
        paymentMethodsList.setLayoutManager(new LinearLayoutManager(requireContext()));

        builder.setView(dialogView);

        cancelButton.setOnClickListener(v -> dismiss());

        return builder.create();
    }

    @Override
    public void onItemClick(String item) {
        listener.onSelectionResponse(item);
        dismiss();
    }

}


