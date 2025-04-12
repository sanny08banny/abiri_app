package com.sanny_tech.carapp.adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.databinding.SearchCarItemBinding;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.hire_utils.Hire;
import com.sanny_tech.carapp.utils.IpAddressManager;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class SearchCarAdapter extends RecyclerView.Adapter<SearchCarAdapter.SearchCarViewHolder> {
    private List<Car> cars;
    private Context context;
    private OnItemClickListener listener;
    private String baseUrl;
    public SearchCarAdapter(List<Car> cars, Context context) {
        this.cars = cars;
        this.context = context;
        this.baseUrl = IpAddressManager.getIpAddress(context);
    }

    public void setItems(List<Car> data) {
        cars.clear();
        cars.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SearchCarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        SearchCarItemBinding searchCarItemBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.search_car_item, parent, false);
        return new SearchCarViewHolder(searchCarItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchCarViewHolder holder, int position) {
        Car bookedCar = cars.get(position);

        holder.bind(bookedCar);
    }

    @Override
    public int getItemCount() {
        return cars.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Car item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class SearchCarViewHolder extends RecyclerView.ViewHolder {
        private SearchCarItemBinding searchCarItemBinding;

        public SearchCarViewHolder(@NonNull SearchCarItemBinding searchCarItemBinding) {
            super(searchCarItemBinding.getRoot());
            this.searchCarItemBinding = searchCarItemBinding;

            searchCarItemBinding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    boolean isExpanded = searchCarItemBinding.swipeLayout.getVisibility() == View.VISIBLE;
                    toggleExpansion(searchCarItemBinding.swipeLayout, isExpanded);
                    return true;
                }
            });
            searchCarItemBinding.hideButtons.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isExpanded = searchCarItemBinding.swipeLayout.getVisibility() == View.VISIBLE;
                    toggleExpansion(searchCarItemBinding.swipeLayout, isExpanded);
                }
            });

        }

        public void bind(Car car) {
            if (car.getCar_images() != null) {
                glideImage(car, searchCarItemBinding.carImage);
            }
            searchCarItemBinding.carDescription.setText(car.getModel());

            searchCarItemBinding.carDescription.setMovementMethod(LinkMovementMethod.getInstance());

            if (car.getDaily_amount() != 0) {
                double amount = Double.parseDouble(String.valueOf(car.getDaily_amount()));
                Locale kenyanLocale = new Locale("sw", "KE");
                Currency kenyanShilling = Currency.getInstance("KES");
                NumberFormat numberFormat = NumberFormat.getCurrencyInstance(kenyanLocale);
                numberFormat.setCurrency(kenyanShilling);
                String formattedAmount = numberFormat.format(amount);

                searchCarItemBinding.price.setText(formattedAmount);
                searchCarItemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(car);
                    }
                });
            }
        }
        private void glideImage(Car car, ImageView imageView) {
            if (car != null) {
                String endPoint = baseUrl + "/car/" + car.getOwner_id() + "/"
                        + car.getCar_id() + "/" + car.getCar_images().get(0);
                Glide.with(context).asBitmap().load(endPoint)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.baseline_downloading_350) // Placeholder image while loading
                                .error(R.drawable.baseline_downloading_350)      // Error image if loading fails
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .override(ViewGroup.LayoutParams.MATCH_PARENT, 500)
                        .into(imageView);
            }
        }
        private void deleteCar(Hire car) {
//            BookCarLoader bookCarLoader = new BookCarLoader(context, car.getCar_id(), ActionType.DELETE, null);
//            showLoadingState();
//            bookCarLoader.forceLoad();
//            bookCarLoader.registerListener(7, new Loader.OnLoadCompleteListener<String>() {
//                @Override
//                public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
//                    hideLoadingState();
//                    if (data != null) {
//                        Toast.makeText(context, "Successful connect", Toast.LENGTH_SHORT).show();
//                        databaseHelper.deleteBookedCarByCarId(car.getCar_id());
//                        cars.remove(car);
//                        notifyDataSetChanged();
//                    }
//                }
//            });
        }

        private void showLoadingState() {
            searchCarItemBinding.deleteButton.setVisibility(View.GONE);
        }

        private void hideLoadingState() {
            searchCarItemBinding.deleteButton.setVisibility(View.VISIBLE);
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
}
