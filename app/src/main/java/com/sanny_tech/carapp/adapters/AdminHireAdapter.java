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
import com.sanny_tech.carapp.databasehelpers.UploadedCarsHelper;
import com.sanny_tech.carapp.databinding.AdminHireItemBinding;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.entities.CarBookRequest;
import com.sanny_tech.carapp.entities.NewBookingRequest;
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

public class AdminHireAdapter extends RecyclerView.Adapter<AdminHireAdapter.ViewHolder> implements
        HireStatusChecker.OnHireChangedListener {

    private Context context;
    private List<Car> carList;
    private String baseUrl;
    private List<Hire> hires;
    private Handler handler;
    private Runnable updateCountdownRunnable;
    private OnTaskDoneListener listener;

    public AdminHireAdapter(Context context, List<Car> carList, List<Hire> hires) {
        this.context = context;
        this.carList = carList;
        this.baseUrl = IpAddressManager.getIpAddress(context);
        this.hires = hires;
    }
    public interface OnTaskDoneListener {
        void onTaskDone(String item);
    }

    public void setOnTaskDoneListener(OnTaskDoneListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        AdminHireItemBinding hireItemBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.admin_hire_item, parent, false);
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
        private AdminHireItemBinding hireItemBinding;

        public ViewHolder(@NonNull AdminHireItemBinding adminHireItemBinding) {
            super(adminHireItemBinding.getRoot());
            this.hireItemBinding = adminHireItemBinding;
        }

        void bind(Hire hire, Car car) {
            hireItemBinding.model.setText(car.getModel());
            hireItemBinding.name.setText(hire.getClient());
            hireItemBinding.phoneNumber.setText(hire.getClient_contact());
            hireItemBinding.plate.setText(car.getCar_id());
            if (hire.getStart_date() != null) {
                hireItemBinding.date.setText(MessageFormat.format("{0} - ",
                        formatTime(Long.parseLong(hire.getStart_date()))));
                if (hire.getEnd_date() != null) {
                    hireItemBinding.date.setText(MessageFormat.format("{0} - {1}",
                            formatTime(Long.parseLong(hire.getStart_date())),
                            formatTime(Long.parseLong(hire.getEnd_date()))));
                }
            }
            glideImage(car, hireItemBinding.imageView);
            if (hire.getStatus().equals("verified")) {
                hireItemBinding.acceptLt.setVisibility(View.GONE);
            } else if (hire.getStatus().equals("initialised")) {
                hireItemBinding.completeLt.setVisibility(View.GONE);
            } else {
                try {
                    Long time = Long.parseLong(hire.getStatus());
                    hireItemBinding.acceptLt.setVisibility(View.GONE);
                    loadPenaltyPeriod(hire, hireItemBinding.startPenalty);
                } catch (NumberFormatException ignored) {

                }
            }
            if (Long.parseLong(hire.getEnd_date()) < System.currentTimeMillis()) {
                hireItemBinding.completeLt.setVisibility(View.VISIBLE);
            }
            hireItemBinding.complete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openCompleteHire(hire);
                }
            });
            hireItemBinding.deny.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    declineBookingRequest(hire);
                }
            });
            hireItemBinding.approve.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bookCar(hire);
                }
            });
            hireItemBinding.startPenalty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPenaltyConfirmationDialog(hire);
                }
            });
        }

        private void showPenaltyConfirmationDialog(Hire car) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Protect your car!");
            builder.setMessage("Are you sure you want to start penalty for this car?\n " +
                    "This action will charge client until you confirm car is returned by completing hire.");
            builder.setPositiveButton("Start Penalty", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startPenalty(car);
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        }

        private void loadPenaltyPeriod(Hire hire, MaterialButton button) {
            handler = new Handler();

            // Initialize the countdown update runnable
            updateCountdownRunnable = new Runnable() {
                @Override
                public void run() {
                    button.setText(getFormattedElapsedTime(hire));
                    handler.postDelayed(this, 1000); // Update every second
                }
            };

            // Start the countdown updates
            handler.post(updateCountdownRunnable);
        }

        public long getElapsedTripTime(Hire hire) {
            long startTime = Long.parseLong(hire.getStatus());
            if (startTime > 0) {
                return System.currentTimeMillis() - startTime;
            } else {
                return 0;
            }
        }

        public String getFormattedElapsedTime(Hire hire) {
            long elapsedMillis = getElapsedTripTime(hire);
            long seconds = elapsedMillis / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;

            // Format the elapsed time into a readable string
            String formattedTime = String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
            return formattedTime;
        }

        private void startPenalty(Hire hire) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("hires");
            hire.setOwner(getCurrentAccountUserName());
            hire.setOwner_contact(SimCardManager.getPhoneNumber(context));
            hire.setStatus(String.valueOf(System.currentTimeMillis()));
            reference.child(hire.getId()).setValue(hire);
            makeCarUnavailable();
            Toast.makeText(context, "Penalty started successfully",
                    Toast.LENGTH_SHORT).show();
            listener.onTaskDone("done");
        }

        private void bookCar(Hire hire) {
            Toast.makeText(context, "Please wait ...", Toast.LENGTH_SHORT).show();
            Car car = hire.getCar();
            NewBookingRequest bookingRequest = new NewBookingRequest(
                    hire.getClient_id(),car.getCar_id(), car.getOwner_id(), "Accept",
                    formatTime1(Long.parseLong(hire.getStart_date())),formatTime1(Long.parseLong(hire.getEnd_date())));
            BookCarLoader bookCarLoader = new BookCarLoader(context,
                    bookingRequest, ActionType.ACCEPT_BOOK);
            bookCarLoader.forceLoad();
            bookCarLoader.registerListener(798, new Loader.OnLoadCompleteListener<String>() {
                @Override
                public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                    if (data != null) {
                        acceptHire(hire);
                    } else {
                        Toast.makeText(context, "Unsuccessful booking",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        private void acceptHire(Hire hire) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("hires");
            hire.setOwner(getCurrentAccountUserName());
            hire.setOwner_contact(SimCardManager.getPhoneNumber(context));
            hire.setStatus("verified");
            reference.child(hire.getId()).setValue(hire);
            makeCarUnavailable();
            Toast.makeText(context, "Hew hire created successful",
                    Toast.LENGTH_SHORT).show();
            listener.onTaskDone("done");
        }
        private void declineBookingRequest(Hire hire) {
            Toast.makeText(context, "Please wait ...", Toast.LENGTH_SHORT).show();
            Car car = hire.getCar();
            if (car != null) {
                NewBookingRequest bookingRequest = new NewBookingRequest(
                        hire.getClient_id(),car.getCar_id(), car.getOwner_id(), "Decline",
                        formatTime1(Long.parseLong(hire.getStart_date())),formatTime1(Long.parseLong(hire.getEnd_date())));
                BookCarLoader bookCarLoader = new BookCarLoader(context
                        , bookingRequest,
                        ActionType.DECLINE);
                bookCarLoader.forceLoad();
                bookCarLoader.registerListener(8, new Loader.OnLoadCompleteListener<String>() {
                    @Override
                    public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                        if (data != null) {
                            declineHire(hire);
                        } else {
                            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        private String formatTime1(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
        private void declineHire(Hire hire) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("hires");
            hire.setOwner(getCurrentAccountUserName());
            hire.setOwner_contact(SimCardManager.getPhoneNumber(context));
            hire.setStatus("declined");
            reference.child(hire.getId()).setValue(hire);
            makeCarUnavailable();
            Toast.makeText(context, "Hire decline successful", Toast.LENGTH_SHORT).show();
            listener.onTaskDone("done");
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

        private void makeCarUnavailable() {
        }

        private void openCompleteHire(Hire hire) {
            Intent tripIntent = new Intent(context, TripActivity.class);
            tripIntent.putExtra("hireId", hire.getId());  // Pass any necessary data
            context.startActivity(tripIntent);
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

