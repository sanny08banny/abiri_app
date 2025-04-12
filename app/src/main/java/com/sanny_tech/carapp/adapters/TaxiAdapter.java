package com.sanny_tech.carapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.databinding.TaxiItemBinding;
import com.sanny_tech.carapp.taxi_utils.Vehicle;
import com.sanny_tech.carapp.utils.IpAddressManager;


import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class TaxiAdapter extends RecyclerView.Adapter<TaxiAdapter.TaxiViewHolder> {
    private List<Vehicle> vehicles;
    private Context context;
    private OnItemClickListener listener;
    private String baseUrl;
    private double currentLatitude, currentLongitude, travelDistance;
    private int selectedPosition = -1;

    public TaxiAdapter(List<Vehicle> vehicles, Context context, double currentLatitude, double currentLongitude, double travelDistance) {
        this.vehicles = vehicles;
        this.context = context;
        this.baseUrl = IpAddressManager.getIpAddress(context);
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.travelDistance = travelDistance;
    }

    public void setItems(List<Vehicle> data) {
        vehicles.clear();
        vehicles.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaxiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        TaxiItemBinding taxiItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.taxi_item, parent, false);
        return new TaxiViewHolder(taxiItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaxiViewHolder holder, int position) {
        Vehicle vehicle = vehicles.get(position);

        boolean hasLocations = !vehicle.getTaxiLocations().isEmpty();

        holder.itemView.setBackgroundResource(
                position == selectedPosition ? R.drawable.item_background_selected :
                        R.drawable.item_background_default);

        holder.itemView.setOnClickListener(v -> {
            if (hasLocations) {
                int previousSelectedPosition = selectedPosition;
                selectedPosition = holder.getBindingAdapterPosition();
                notifyItemChanged(previousSelectedPosition);
                notifyItemChanged(selectedPosition);
                listener.onItemClick(vehicle);
            }
        });

        holder.itemView.setAlpha(hasLocations ? 1.0f : 0.5f); // Set transparency for disabled items
        holder.bind(vehicle, position, hasLocations);
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Vehicle item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class TaxiViewHolder extends RecyclerView.ViewHolder {
        private TaxiItemBinding taxiItemBinding;

        public TaxiViewHolder(@NonNull TaxiItemBinding taxiItemBinding) {
            super(taxiItemBinding.getRoot());
            this.taxiItemBinding = taxiItemBinding;
        }

        public void bind(Vehicle vehicle, int position, boolean hasLocations) {
            taxiItemBinding.taxiDesc.setText(vehicle.getCategory());
            taxiItemBinding.seatCount.setText(String.valueOf(vehicle.getSeat_count()));

            if (hasLocations) {
                taxiItemBinding.price.setText(formatAmount(vehicle.getPrice()));
            } else {
                taxiItemBinding.price.setText(formatAmount(vehicle.getPrice()));
            }

            if (vehicle.getCategory().equals("BodaBoda")) {
                taxiItemBinding.taxiDesc.setText("Boda Boda");
                taxiItemBinding.commentProfileImage.setImageResource(R.drawable.bike);
            } else {
                taxiItemBinding.commentProfileImage.setImageResource(R.drawable.car_1);
            }
        }

        private String formatAmount(double price) {
            Locale kenyanLocale = new Locale("sw", "KE");
            Currency kenyanShilling = Currency.getInstance("KES");
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(kenyanLocale);
            numberFormat.setCurrency(kenyanShilling);
            return numberFormat.format(price);
        }
    }
}

