package com.sanny_tech.carapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.databinding.PaymentMethodBinding;
import com.sanny_tech.carapp.utils.IpAddressManager;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {
    private List<String> payments;
    private Context context;
    private OnItemClickListener listener;
    private String baseUrl;
    public PaymentAdapter(List<String> payments, Context context) {
        this.payments = payments;
        this.context = context;
        this.baseUrl = IpAddressManager.getIpAddress(context);
    }
    public void setItems(List<String> data) {
        payments.clear();
        payments.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        PaymentMethodBinding paymentMethodBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.payment_method,parent,false);
        return new PaymentViewHolder(paymentMethodBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        String payment = payments.get(position);

        holder.bind(payment);
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    public interface OnItemClickListener {
        void onItemClick(String item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class PaymentViewHolder extends RecyclerView.ViewHolder{
        private PaymentMethodBinding paymentMethodBinding;

        public PaymentViewHolder(@NonNull PaymentMethodBinding paymentMethodBinding) {
            super(paymentMethodBinding.getRoot());
            this.paymentMethodBinding = paymentMethodBinding;
        }
        public void bind(String payment) {
            if (payment.equals("Cash")){
                paymentMethodBinding.paymentType.setText(payment + " (Default)");
            }
            paymentMethodBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(payment);
                }
            });
        }
        private String formatTime(String timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }

    private String calculatePrice(double travelDistance) {
        double price = travelDistance * 50;

        Locale kenyanLocale = new Locale("sw", "KE");
        Currency kenyanShilling = Currency.getInstance("KES");
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(kenyanLocale);
        numberFormat.setCurrency(kenyanShilling);
        String formattedAmount = numberFormat.format(price);
        return formattedAmount;
    }
}
