package com.sanny_tech.carapp.adapters;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.AboutCarActivity;
import com.sanny_tech.carapp.asynctasks.BookCarLoader;
import com.sanny_tech.carapp.databinding.AdminHireCompleteBinding;
import com.sanny_tech.carapp.databinding.AdminHireItemBinding;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.enums.ActionType;
import com.sanny_tech.carapp.hire_utils.Hire;
import com.sanny_tech.carapp.hire_utils.HireStatusChecker;
import com.sanny_tech.carapp.taxi_utils.TripActivity;
import com.sanny_tech.carapp.utils.IpAddressManager;
import com.sanny_tech.carapp.utils.SimCardManager;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminHireCompleteAdapter extends RecyclerView.Adapter<AdminHireCompleteAdapter.ViewHolder> implements
        HireStatusChecker.OnHireChangedListener {

    private Context context;
    private List<Car> carList;
    private String baseUrl;
    private List<Hire> hires;
    private Handler handler;
    private Runnable updateCountdownRunnable;

    public AdminHireCompleteAdapter(Context context, List<Car> carList, List<Hire> hires) {
        this.context = context;
        this.carList = carList;
        this.baseUrl = IpAddressManager.getIpAddress(context);
        this.hires = hires;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        AdminHireCompleteBinding hireItemBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.admin_hire_complete, parent, false);
        return new ViewHolder(hireItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hire hire = hires.get(position);
        Car selectedCar = null;
        for (Car car : carList) {
            if (car.getCar_id().equals(hire.getCarId())) {
                selectedCar = car;
            }
        }
        if (selectedCar != null) {
            holder.bind(hire, selectedCar);
        }
    }

    @Override
    public int getItemCount() {
        return hires.size();
    }

    public void setItems(List<Hire> data) {
        hires.clear();
        hires.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public void onRideChanged(List<String> hires) {
        hires.addAll(hires);
        Log.e("hired cars1", String.valueOf(hires.size()));

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private AdminHireCompleteBinding hireItemBinding;

        public ViewHolder(@NonNull AdminHireCompleteBinding adminHireItemBinding) {
            super(adminHireItemBinding.getRoot());
            this.hireItemBinding = adminHireItemBinding;
        }

        void bind(Hire hire, Car car) {
            hireItemBinding.model.setText(car.getModel());
            hireItemBinding.durationButton.setText(hire.getClient());
            glideImage(car,hireItemBinding.carImage);
            if (hire.getStart_date() != null) {
                hireItemBinding.date.setText(MessageFormat.format("{0} - ",
                        formatTime(Long.parseLong(hire.getStart_date()))));
                if (hire.getEnd_date() != null) {
                    hireItemBinding.date.setText(MessageFormat.format("{0} - {1}",
                            formatTime(Long.parseLong(hire.getStart_date())),
                            formatTime(Long.parseLong(hire.getEnd_date()))));
                }
            }
        }

        public String getCurrentAccountUserName() {
            SharedPreferences sharedPreferences = context.getSharedPreferences("AccountPrefs", MODE_PRIVATE);
            return sharedPreferences.getString("currentUserName", null);
        }

        public String getCurrentAccountId() {
            SharedPreferences sharedPreferences = context.getSharedPreferences("AccountPrefs",
                    MODE_PRIVATE);
            return sharedPreferences.getString("currentUserId", null);
        }
        private String formatTime(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }

        private void glideImage(Car car, ImageView imageView) {
            if (car != null) {
                String endPoint = baseUrl + "/car/image/" + car.getOwner_id() + "/"
                        + car.getCar_id() + "/" + car.getCar_images().get(0);
                // Log or print the endpoint for debugging
                Log.d("ImageEndpoint", "Loading image from: " + endPoint);

                Glide.with(context)
                        .load(endPoint)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.carpooling)
                                .error(R.drawable.baseline_downloading_350) // Error image if loading fails
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .override(ViewGroup.LayoutParams.MATCH_PARENT, 500)
                        .into(imageView);
            }
        }

    }

    private void openAboutCar(Car car) {
        Intent intent = new Intent(context, AboutCarActivity.class);
        intent.putExtra("selectedCar", car);
        intent.putExtra("instruction", "local");
        context.startActivity(intent);
    }

}

