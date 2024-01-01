package com.sanny_tech.carapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
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
import com.sanny_tech.carapp.databinding.MassueseItemBinding;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.utils.FavouritesManager;
import com.sanny_tech.carapp.utils.IpAddressManager;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MasseuseAdapter extends RecyclerView.Adapter<MasseuseAdapter.ViewHolder> {

    private Context context;
    private List<Car> carList;
    private String baseUrl;
    private OnItemClickListener listener;

    public MasseuseAdapter(Context context, List<Car> carList) {
        this.context = context;
        this.carList = carList;
        this.baseUrl = IpAddressManager.getIpAddress(context);
    }

    public List<Car> getCars() {
        if (carList.size() != 0) {
            return carList;
        }
        return null;
    }
    public void checkCarAvailability() {
        for (Car car : carList){
            if (car.getAvailable().equals("Unavailable")){

            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Car item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        MassueseItemBinding massueseItemBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.massuese_item, parent, false);
        return new ViewHolder(massueseItemBinding);
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

    public void setItems(List<Car> data) {
        carList.clear();
        carList.addAll(data);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private MassueseItemBinding massueseItemBinding;

        public ViewHolder(@NonNull MassueseItemBinding massueseItemBinding) {
            super(massueseItemBinding.getRoot());
            this.massueseItemBinding = massueseItemBinding;
        }

        void bind(Car car) {
            Log.e(car.getCar_id(),car.getAvailable());
            massueseItemBinding.location.setText(car.getLocation());

            if (car.getCar_images() != null) {
                glideImage(car, massueseItemBinding.imageView);
            }
            massueseItemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAboutCar(car);
                }
            });
            massueseItemBinding.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAboutCar(car);
                }
            });
            massueseItemBinding.options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showOptionsMenu(massueseItemBinding.options,car);
                }
            });

        }

        private void glideImage(Car car, ImageView imageView) {
            if (car != null) {
                String endPoint = baseUrl + "/car/" + car.getOwner_id() + "/"
                        + car.getCar_id() + "/" + car.getCar_images().get(0);
                Glide.with(context).asBitmap().load(R.drawable.masseuse)
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

//                                // Generate color palette
//                                Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
//                                    @Override
//                                    public void onGenerated(@Nullable Palette palette) {
//                                        if (palette != null) {
//                                            int vibrantColor = palette.getDominantColor(0xFF000000);
//
//                                            // Set the dynamic end color of the gradient overlay
//                                            GradientDrawable gradientDrawable = (GradientDrawable) gradientView.getBackground();
//                                            gradientDrawable.setColors(new int[]{vibrantColor, vibrantColor});
//                                        }
//                                    }
//                                });
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                // Clear any previous loaded resources if needed
                            }
                        });
            }
        }
        private void showOptionsMenu(View view, Car car) {
            Context wrapper = new ContextThemeWrapper(context, R.style.PopupMenuStyle);
            PopupMenu popupMenu = new PopupMenu(wrapper, view);
            popupMenu.getMenuInflater().inflate(R.menu.item_options_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.review) {
                    listener.onItemClick(car);
                    return true;
                } else if (itemId == R.id.fav) {
                    // Implement edit action
                    FavouritesManager.addQuery(context,car.getCar_id());
                    Toast.makeText(context, car.getModel() + " added successfully", Toast.LENGTH_LONG).show();
                    return true;
                } else {
                    return false;
                }
            });
            popupMenu.show();
        }


    }

    private void openAboutCar(Car car) {
        Intent intent = new Intent(context, AboutCarActivity.class);
        intent.putExtra("selectedCar", car);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.scale_up, R.anim.scale_down);
        }
    }

    private void bookCar(Car car, long fromDateMillis, long toDateMillis) {
        Intent intent = new Intent(context, BookedActivity.class);
        intent.setAction("book car");
        intent.putExtra("car", car);
        intent.putExtra("from", fromDateMillis);
        intent.putExtra("to", toDateMillis);
        context.startActivity(intent);
    }

    private void showDatePickerDialog(Context context, Car car) {
        // Create a MaterialDatePicker for selecting a date range
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date Range")
                .setSelection(Pair.create(System.currentTimeMillis(), System.currentTimeMillis())) // Initial selection (today)
                .build();

        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long, Long> selection) {
                long fromDateMillis = selection.first;
                long toDateMillis = selection.second;

                // Convert milliseconds to a duration string
                long durationMillis = toDateMillis - fromDateMillis;
                long days = TimeUnit.MILLISECONDS.toDays(durationMillis);
                long hours = TimeUnit.MILLISECONDS.toHours(durationMillis) - TimeUnit.DAYS.toHours(days);
                String duration = String.format(Locale.US, "%d days", days);

                // Call the bookCar method with the car and duration
                bookCar(car, fromDateMillis,toDateMillis);
            }
        });


        picker.show(((AppCompatActivity) context).getSupportFragmentManager(), picker.toString());
    }
}

