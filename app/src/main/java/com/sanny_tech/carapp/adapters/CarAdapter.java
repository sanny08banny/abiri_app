package com.sanny_tech.carapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcel;
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
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.datepicker.CalendarConstraints;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.AboutCarActivity;
import com.sanny_tech.carapp.activities.BookedActivity;
import com.sanny_tech.carapp.databinding.CarItemBinding;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.hire_utils.HireActivity;
import com.sanny_tech.carapp.utils.FavouritesManager;
import com.sanny_tech.carapp.utils.IpAddressManager;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.ViewHolder> {

    private Context context;
    private List<Car> carList;
    private String baseUrl;
    private OnItemClickListener listener;

    public CarAdapter(Context context, List<Car> carList) {
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
//    public void checkCarAvailability() {
//        for (Car car : carList){
//            if (car.getAvailable().equals("Unavailable")){
//
//            }
//        }
//    }

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
        CarItemBinding carItemBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.car_item, parent, false);
        return new ViewHolder(carItemBinding);
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
        private CarItemBinding carItemBinding;

        public ViewHolder(@NonNull CarItemBinding carItemBinding) {
            super(carItemBinding.getRoot());
            this.carItemBinding = carItemBinding;
        }

        void bind(Car car) {
            carItemBinding.location.setText(car.getLocation());
            carItemBinding.model.setText(car.getModel());
            carItemBinding.description.setText(car.getDescription());
            carItemBinding.imageView.setImageDrawable(null); // Clear previous
            carItemBinding.imageView.setBackgroundResource(R.drawable.static_shimmer_placeholder);
            if (car.getCar_images() != null) {
                glideImage(car, carItemBinding.imageView, carItemBinding.price);
            }

            // Only daily pricing is available, show the daily booking button
            carItemBinding.price.setText(MessageFormat.format("Book at {0}/day", car.getDaily_amount()));
            carItemBinding.price.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePickerDialog(context, car);
                }
            });
            carItemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAboutCar(car);
                }
            });
            carItemBinding.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAboutCar(car);
                }
            });
            carItemBinding.options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showOptionsMenu(carItemBinding.options,car);
                }
            });

        }

        private void glideImage(Car car, ImageView imageView, View gradientView) {
            if (car != null && car.getCar_images() != null && !car.getCar_images().isEmpty()) {
                // Set shimmer background before loading

                String endPoint = baseUrl + "/car/image/" + car.getOwner_id() + "/"
                        + car.getCar_id() + "/" + car.getCar_images().get(0);

                Glide.with(context)
                        .asBitmap()
                        .load(endPoint)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.baseline_downloading_350)
                                .error(R.drawable.baseline_downloading_350)
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .override(ViewGroup.LayoutParams.MATCH_PARENT, 500)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                imageView.setImageBitmap(resource);
                                imageView.setBackground(null); // Remove shimmer placeholder

                                // Maintain aspect ratio
                                float aspectRatio = (float) resource.getWidth() / resource.getHeight();
                                int desiredHeight = (int) (imageView.getWidth() / aspectRatio);
                                ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                                layoutParams.height = desiredHeight;
                                imageView.setLayoutParams(layoutParams);

                                // Extract dominant color
                                Palette.from(resource).generate(palette -> {
                                    if (palette != null) {
                                        int vibrantColor = palette.getDominantColor(0xFF000000);
                                        GradientDrawable gradientDrawable = (GradientDrawable) gradientView.getBackground();
                                        gradientDrawable.setColors(new int[]{vibrantColor, vibrantColor});
                                    }
                                });
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                imageView.setBackground(null); // Remove shimmer on clear
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                imageView.setBackground(null); // Remove shimmer on error
                                imageView.setImageDrawable(errorDrawable);
                            }
                        });
            }
        }
        private String convertText(String amount) {
            // Remove commas from the input amount
            return amount.replace(" ", "");
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
        Intent intent = new Intent(context, HireActivity.class);
        intent.setAction("book car");
        intent.putExtra("car", car);
        intent.putExtra("from", fromDateMillis);
        intent.putExtra("to", toDateMillis);
        context.startActivity(intent);
    }

//    private void showDatePickerDialog(Context context, Car car) {
//        // Create a MaterialDatePicker for selecting a date range
//        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
//                .setTitleText("Select Date Range")
//                .setSelection(Pair.create(System.currentTimeMillis(), System.currentTimeMillis())) // Initial selection (today)
//                .build();
//
//        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
//            @Override
//            public void onPositiveButtonClick(Pair<Long, Long> selection) {
//                long fromDateMillis = selection.first;
//                long toDateMillis = selection.second;
//
//                // Convert milliseconds to a duration string
//                long durationMillis = toDateMillis - fromDateMillis;
//                long days = TimeUnit.MILLISECONDS.toDays(durationMillis);
//                long hours = TimeUnit.MILLISECONDS.toHours(durationMillis) - TimeUnit.DAYS.toHours(days);
//                String duration = String.format(Locale.US, "%d days", days);
//
//                // Call the bookCar method with the car and duration
//                bookCar(car, fromDateMillis,toDateMillis);
//            }
//        });
//
//
//        picker.show(((AppCompatActivity) context).getSupportFragmentManager(), picker.toString());
//    }
private void showDatePickerDialog(Context context, Car car) {
    // Parse car's unavailable dates to a Set of UTC-normalized timestamps
    Set<Long> unavailableTimestamps = new HashSet<>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Force UTC parsing

    for (String dateStr : car.getUnavailable_dates()) {
        try {
            Date date = sdf.parse(dateStr);
            if (date != null) {
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                cal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                unavailableTimestamps.add(cal.getTimeInMillis());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // Validator to block unavailable and past dates
    CalendarConstraints.DateValidator validator = new CalendarConstraints.DateValidator() {
        @Override
        public boolean isValid(long date) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis(date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long normalizedDate = cal.getTimeInMillis();

            Calendar todayCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);
            long today = todayCal.getTimeInMillis();

            return normalizedDate >= today && !unavailableTimestamps.contains(normalizedDate);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {}
    };

    CalendarConstraints constraints = new CalendarConstraints.Builder()
            .setValidator(validator)
            .build();

    // Create and show the MaterialDatePicker
    MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Available Date Range")
            .setCalendarConstraints(constraints)
            .setSelection(Pair.create(System.currentTimeMillis(), System.currentTimeMillis()))
            .build();

    picker.addOnPositiveButtonClickListener(selection -> {
        long fromDateMillis = selection.first;
        long toDateMillis = selection.second;

        boolean valid = true;

        for (long millis = fromDateMillis; millis <= toDateMillis; millis += TimeUnit.DAYS.toMillis(1)) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis(millis);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            if (unavailableTimestamps.contains(cal.getTimeInMillis())) {
                valid = false;
                break;
            }
        }

        if (!valid) {
            Toast.makeText(context, "One or more selected days are unavailable", Toast.LENGTH_LONG).show();
            return;
        }

        long durationMillis = toDateMillis - fromDateMillis;
        long days = TimeUnit.MILLISECONDS.toDays(durationMillis);

        // Proceed with booking
        bookCar(car, fromDateMillis, toDateMillis);
    });

    picker.show(((AppCompatActivity) context).getSupportFragmentManager(), picker.toString());
}

}

