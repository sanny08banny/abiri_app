package com.sanny_tech.carapp.adapters;

import static android.content.Context.MODE_PRIVATE;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.asynctasks.BookCarLoader;
import com.sanny_tech.carapp.databinding.BookedCarItemBinding;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.entities.NewBookingRequest;
import com.sanny_tech.carapp.enums.ActionType;
import com.sanny_tech.carapp.hire_utils.Hire;
import com.sanny_tech.carapp.utils.IpAddressManager;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BookedCarAdapter extends RecyclerView.Adapter<BookedCarAdapter.BookedCarViewHolder> {
    private List<Hire> hires;
    private Context context;
    private OnItemClickListener listener;
    private String baseUrl;

    public BookedCarAdapter(List<Hire> hires, Context context) {
        this.hires = hires;
        this.context = context;
        this.baseUrl = IpAddressManager.getIpAddress(context);
    }

    public void setItems(List<Hire> data) {
        hires.clear();
        hires.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookedCarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        BookedCarItemBinding bookedCarItemBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.booked_car_item, parent, false);
        return new BookedCarViewHolder(bookedCarItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookedCarViewHolder holder, int position) {
        Hire hire = hires.get(position);

        holder.bind(hire);
    }

    @Override
    public int getItemCount() {
        return hires.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Hire item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class BookedCarViewHolder extends RecyclerView.ViewHolder {
        private BookedCarItemBinding bookedCarItemBinding;

        public BookedCarViewHolder(@NonNull BookedCarItemBinding bookedCarItemBinding) {
            super(bookedCarItemBinding.getRoot());
            this.bookedCarItemBinding = bookedCarItemBinding;

            bookedCarItemBinding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    boolean isExpanded = bookedCarItemBinding.swipeLayout.getVisibility() == View.VISIBLE;
                    toggleExpansion(bookedCarItemBinding.swipeLayout, isExpanded);
                    return true;
                }
            });
            bookedCarItemBinding.hideButtons.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isExpanded = bookedCarItemBinding.swipeLayout.getVisibility() == View.VISIBLE;
                    toggleExpansion(bookedCarItemBinding.swipeLayout, isExpanded);
                }
            });

        }

        public void bind(Hire hire) {
            if (hire.getCar() != null && hire.getCar().getCar_images() != null) {
                String image = hire.getCar().getCar_images().get(0);
                String endPoint = baseUrl + "/car/image/" + hire.getOwner_id() + "/"
                        + hire.getCar().getCar_id() + "/" + image;
                Glide.with(context)
                        .asBitmap()
                        .load(endPoint)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.baseline_downloading_350) // Placeholder image while loading
                                .error(R.drawable.loading)      // Error image if loading fails
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .override(ViewGroup.LayoutParams.MATCH_PARENT, 500)
                        .into(bookedCarItemBinding.carImage);
            }
            if (hire.getStatus().equals("complete")){
                bookedCarItemBinding.completeStatus.setVisibility(View.VISIBLE);
            } else if (hire.getStatus().equals("initialised")) {
                bookedCarItemBinding.completeStatus.setVisibility(View.VISIBLE);
                bookedCarItemBinding.completeText.setText("Waiting verification");
                bookedCarItemBinding.paymentsButton.setImageResource(R.drawable.loading);
            }
            bookedCarItemBinding.name.setText(hire.getOwner());
            bookedCarItemBinding.phoneNumber.setText(hire.getOwner_contact());
            bookedCarItemBinding.date.setText(MessageFormat.format("{0} - {1}",
                    formatTime(Long.parseLong(hire.getStart_date())),
                    formatTime(Long.parseLong(hire.getEnd_date()))));
            if (Long.parseLong(hire.getEnd_date()) < System.currentTimeMillis()){
                try {
                    Long time = Long.parseLong(hire.getStatus());
                    bookedCarItemBinding.penaltyAlert.setVisibility(View.VISIBLE);
                    bookedCarItemBinding.mainLt.setBackgroundColor(context.getColor(
                            R.color.red));
                } catch (NumberFormatException ignored) {

                }
            }
            long durationMillis = Long.parseLong(hire.getEnd_date()) -
                    Long.parseLong(hire.getStart_date());
            long days = TimeUnit.MILLISECONDS.toDays(durationMillis);
            long hours = TimeUnit.MILLISECONDS.toHours(durationMillis) - TimeUnit.DAYS.toHours(days);
            String duration = String.format(Locale.US, "%d days", days);
            String attendantName = "[CHANGE PREFERENCES]"; // Replace with the actual method to get the name

// Create a SpannableString for the rental price
            SpannableString rentalDurationSpannable = new SpannableString(duration);
            ClickableSpan rentalPriceClickSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    // Handle click action for the rental price here
                    // For example, you can open a dialog or perform some action
                }
            };
            rentalDurationSpannable.setSpan(rentalPriceClickSpan, 0, duration.length(),
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

// Create a SpannableString for the attendant name
            SpannableString attendantNameSpannable = new SpannableString(attendantName);
            ClickableSpan attendantNameClickSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    boolean isExpanded = bookedCarItemBinding.swipeLayout.getVisibility() == View.VISIBLE;
                    toggleExpansion(bookedCarItemBinding.swipeLayout, isExpanded);
                }
            };
            attendantNameSpannable.setSpan(attendantNameClickSpan, 0, attendantName.length(),
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

// Create a SpannableStringBuilder to combine the formatted text
            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append("Car rented for ");
            builder.append(rentalDurationSpannable);
            builder.append(". Long click on this item to change preferences.");
            builder.append(attendantNameSpannable);

            bookedCarItemBinding.carDescription.setText(builder);

            bookedCarItemBinding.carDescription.setMovementMethod(LinkMovementMethod.getInstance());

            Locale kenyanLocale = new Locale("sw", "KE");
            Currency kenyanShilling = Currency.getInstance("KES");
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(kenyanLocale);
            numberFormat.setCurrency(kenyanShilling);
            String formattedAmount = numberFormat.format(hire.getCharges());

            bookedCarItemBinding.price.setText(formattedAmount);

            bookedCarItemBinding.configureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isExpanded = bookedCarItemBinding.detailsLt.getVisibility() == View.VISIBLE;
                    toggleExpansion(bookedCarItemBinding.detailsLt, isExpanded);
                }
            });
            bookedCarItemBinding.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmationDialog(hire);
                }
            });
            bookedCarItemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(hire);
                }
            });
        }

        private void deleteCar(Hire hire) {
            Car car = hire.getCar();
            if (car != null) {
                NewBookingRequest bookingRequest = new NewBookingRequest(
                        getCurrentAccountId(), car.getCar_id(), car.getOwner_id(), "Cancel",
                        formatTime1(Long.parseLong(hire.getStart_date())), formatTime1(Long.parseLong(hire.getEnd_date())));

                BookCarLoader bookCarLoader = new BookCarLoader(context, bookingRequest,
                        ActionType.DELETE);
                showLoadingState();
                bookCarLoader.forceLoad();
                bookCarLoader.registerListener(7, new Loader.OnLoadCompleteListener<String>() {
                    @Override
                    public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                        hideLoadingState();
                        if (data != null) {
                            Toast.makeText(context, "Successful connect", Toast.LENGTH_SHORT).show();
                            DatabaseReference reference = FirebaseDatabase.getInstance()
                                    .getReference("hires");
                            reference.child(hire.getId()).removeValue();
                            hires.remove(hire);
                            notifyDataSetChanged();
                        }
                    }
                });
            }
        }
        public String getCurrentAccountId() {
            SharedPreferences sharedPreferences = context.getSharedPreferences("AccountPrefs",
                    MODE_PRIVATE);
            return sharedPreferences.getString("currentUserId", null);
        }
        private String formatTime1(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
        private void showLoadingState() {
            bookedCarItemBinding.deleteButton.setVisibility(View.GONE);
        }

        private void hideLoadingState() {
            bookedCarItemBinding.deleteButton.setVisibility(View.VISIBLE);
        }

        private void showDeleteConfirmationDialog(Hire car) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete Car");
            builder.setMessage("Are you sure you want to delete this car?");
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteCar(car);
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        }
    }

    private void toggleExpansion(View expandLayout, boolean isExpanded) {
        if (isExpanded) {
            expandLayout.setVisibility(View.GONE);
            ObjectAnimator.ofFloat(expandLayout, "scaleY", 1f, 0f)
                    .setDuration(300)
                    .start();
        } else {
            expandLayout.setVisibility(View.VISIBLE);
            ObjectAnimator.ofFloat(expandLayout, "scaleY", 0f, 1f)
                    .setDuration(300)
                    .start();
        }
    }
    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
