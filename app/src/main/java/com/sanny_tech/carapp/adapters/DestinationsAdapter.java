package com.sanny_tech.carapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.databinding.PaymentMethodBinding;
import com.sanny_tech.carapp.fun_utils.SpaceDest;

import java.util.List;

public class DestinationsAdapter extends RecyclerView.Adapter<DestinationsAdapter.DestinationViewHolder> {

    private Context context;
    private List<SpaceDest> companies;
    private String baseUrl;
    private OnItemClickListener listener;

    public DestinationsAdapter(Context context, List<SpaceDest> companies) {
        this.context = context;
        this.companies = companies;
        this.baseUrl = context.getResources().getString(R.string.base_url_title);
    }
    public interface OnItemClickListener {
        void onItemClick(SpaceDest item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public DestinationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        PaymentMethodBinding paymentMethodBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.payment_method,parent,false);
        return new DestinationViewHolder(paymentMethodBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull DestinationViewHolder holder, int position) {
        SpaceDest company = companies.get(position);

        holder.bind(company);
    }

    @Override
    public int getItemCount() {
        return companies.size();
    }

    public void setItems(List<SpaceDest> data) {
        companies.clear();
        companies.addAll(data);
        notifyDataSetChanged();
    }

    public class DestinationViewHolder extends RecyclerView.ViewHolder {
        private PaymentMethodBinding paymentMethodBinding;

        public DestinationViewHolder(@NonNull PaymentMethodBinding paymentMethodBinding) {
            super(paymentMethodBinding.getRoot());
            this.paymentMethodBinding = paymentMethodBinding;
        }
        void bind(SpaceDest dest){
            paymentMethodBinding.paymentType.setText(dest.getName());
            glideImage(dest.getImages_urls().get(0), paymentMethodBinding.image);

            paymentMethodBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(dest);
                }
            });
        }
    }

    private void glideImage(String url, ImageView imageView) {
        if (url != null) {
            String endPoint = baseUrl + "/";
            Glide.with(context)
                    .load(url)
                    .into(imageView);
        }
    }
}

