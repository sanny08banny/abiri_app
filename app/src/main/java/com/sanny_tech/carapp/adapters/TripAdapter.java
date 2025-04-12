package com.sanny_tech.carapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.databinding.TripLtBinding;
import com.sanny_tech.carapp.taxi_utils.Trip;
import com.sanny_tech.carapp.utils.IpAddressManager;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {
    private List<Trip> trips;
    private Context context;
    private OnItemClickListener listener;
    private String baseUrl;
    public TripAdapter(List<Trip> trips, Context context) {
        this.trips = trips;
        this.context = context;
        this.baseUrl = IpAddressManager.getIpAddress(context);
    }
    public void setItems(List<Trip> data) {
        trips.clear();
        trips.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        TripLtBinding tripLtBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.trip_lt,parent,false);
        return new TripViewHolder(tripLtBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = trips.get(position);

        holder.bind(trip);
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Trip item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class TripViewHolder extends RecyclerView.ViewHolder{
        private TripLtBinding tripLtBinding;

        public TripViewHolder(@NonNull TripLtBinding tripLtBinding) {
            super(tripLtBinding.getRoot());
            this.tripLtBinding = tripLtBinding;
        }
        public void bind(Trip trip) {
            tripLtBinding.destination.setText(trip.getDestination());
            String formattedTime = formatTime(trip.getStart_time());
            tripLtBinding.date.setText(formattedTime);

            tripLtBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        listener.onItemClick(trip);
                    }
                }
            });
        }
        private String formatTime(String timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
            return sdf.format(new Date(Long.parseLong(timestamp)));
        }
    }
}
