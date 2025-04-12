package com.sanny_tech.carapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.AboutCarActivity;
import com.sanny_tech.carapp.activities.BookedActivity;
import com.sanny_tech.carapp.databinding.UploadedCarBinding;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.hire_utils.HireStatusChecker;
import com.sanny_tech.carapp.utils.IpAddressManager;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class UploadedCarAdapter extends RecyclerView.Adapter<UploadedCarAdapter.ViewHolder> implements
HireStatusChecker.OnHireChangedListener{

    private Context context;
    private List<Car> carList;
    private String baseUrl;
    private List<String> hiredCars = new ArrayList<>();

    public UploadedCarAdapter(Context context, List<Car> carList) {
        this.context = context;
        this.carList = carList;
        this.baseUrl = IpAddressManager.getIpAddress(context);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        UploadedCarBinding uploadedCarBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.uploaded_car,parent,false);
        return new ViewHolder(uploadedCarBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Car car = carList.get(position);

        holder.bind(car);
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public void setItems( List<String> hires) {
        hiredCars.addAll(hires);
        notifyDataSetChanged();
    }

    @Override
    public void onRideChanged(List<String> hires) {
        hiredCars.addAll(hires);
        Log.e("hired cars1", String.valueOf(hiredCars.size()));

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private UploadedCarBinding uploadedCarBinding;

        public ViewHolder(@NonNull UploadedCarBinding uploadedCarBinding) {
            super(uploadedCarBinding.getRoot());
            this.uploadedCarBinding = uploadedCarBinding;
        }
        void bind(Car car){
            uploadedCarBinding.model.setText(car.getModel());

            if (car.getCar_images() != null) {
                glideImage(car, uploadedCarBinding.imageView);
            }
            if (hiredCars.size() > 0){
                if (hiredCars.contains(car.getCar_id())){
                    uploadedCarBinding.hiredStatus.setVisibility(View.VISIBLE);
                }
            }else {
                Log.e("hired cars", "null");
            }
            uploadedCarBinding.moreDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAboutCar(car);
                }
            });

        }
        private void glideImage(Car car, ImageView imageView) {
            if (car != null) {
                String endPoint = baseUrl + "/car/image/" + car.getOwner_id() + "/"
                        + car.getCar_id() + "/" + car.getCar_images().get(0);
                Glide.with(context).asBitmap().load(endPoint)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.baseline_downloading_350) // Placeholder image while loading
                                .error(R.drawable.baseline_downloading_350)      // Error image if loading fails
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .override(ViewGroup.LayoutParams.MATCH_PARENT, 500)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                // Set the loaded bitmap to the ImageView
                                imageView.setImageBitmap(resource);

                                // Retain the original aspect ratio of the image
                                float aspectRatio = (float) resource.getWidth() / resource.getHeight();

                                // Calculate the desired height based on the original aspect ratio
                                int desiredHeight = (int) (imageView.getWidth() / aspectRatio);

                                // Resize the ImageView to the desired height while keeping the width MATCH_PARENT
                                ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                                layoutParams.height = desiredHeight;
                                imageView.setLayoutParams(layoutParams);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                // Clear any previous loaded resources if needed
                            }
                        });
            }
        }

    }

    private void openAboutCar(Car car) {
        Intent intent = new Intent(context, AboutCarActivity.class);
        intent.putExtra("selectedCar",car);
        intent.putExtra("instruction","local");
        context.startActivity(intent);
    }

}

