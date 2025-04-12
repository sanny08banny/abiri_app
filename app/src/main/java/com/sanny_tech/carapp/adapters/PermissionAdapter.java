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
import com.sanny_tech.carapp.databinding.AdminLtBinding;
import com.sanny_tech.carapp.entities.Permission;
import com.sanny_tech.carapp.entities.TaxiCategory;
import com.sanny_tech.carapp.taxi_utils.DriverAvailabilityManager;

import java.text.MessageFormat;
import java.util.List;

public class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.PermissionViewHolder> {

    private Context context;
    private List<Permission> permissions;
    private String baseUrl;
    private OnItemClickListener listener;
    private String uploadCount;

    public PermissionAdapter(Context context, List<Permission> permissions, String uploadCount) {
        this.context = context;
        this.permissions = permissions;
        this.baseUrl = context.getResources().getString(R.string.base_url_title);
        this.uploadCount = uploadCount;
    }

    @NonNull
    @Override
    public PermissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        AdminLtBinding adminLtBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.admin_lt, parent, false);
        return new PermissionViewHolder(adminLtBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PermissionViewHolder holder, int position) {
        Permission permission = permissions.get(position);

        holder.bind(permission);
    }

    @Override
    public int getItemCount() {
        return permissions.size();
    }

    public void setItems(List<Permission> data) {
        permissions.clear();
        permissions.addAll(data);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(Permission item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class PermissionViewHolder extends RecyclerView.ViewHolder {
        private AdminLtBinding adminLtBinding;

        public PermissionViewHolder(@NonNull AdminLtBinding adminLtBinding) {
            super(adminLtBinding.getRoot());
            this.adminLtBinding = adminLtBinding;
        }

        void bind(Permission permission) {
            adminLtBinding.adminTitle.setText(permission.getName());

            if (!permission.isStatus()) {
                adminLtBinding.requestAdmin.setVisibility(View.VISIBLE);
                adminLtBinding.commentProfileImage.setVisibility(View.GONE);
            }else {
                adminLtBinding.requestAdmin.setVisibility(View.GONE);
                adminLtBinding.commentProfileImage.setVisibility(View.VISIBLE);
            }

            if (permission.getName().equals("Taxi access")) {
                DriverAvailabilityManager availabilityManager = new DriverAvailabilityManager(context);
                if (availabilityManager.getTaxiInit() != null) {
                    adminLtBinding.extras.setText(MessageFormat.format("Current taxi capacity: {0}",
                            TaxiCategory.getNumberOfSeats(
                                    availabilityManager.getTaxiInit().getCategory())));
                }else {
                    adminLtBinding.extras.setText("Setup an account to become a driver and earn as you carry customer");
                }
            } else if (permission.getName().equals("Admin access")) {
                if (uploadCount.length() != 0) {
                    adminLtBinding.extras.setText(MessageFormat.format("{0} cars uploaded", uploadCount));
                }else {
                    adminLtBinding.extras.setText("Manage your requests bookings seamlessly and communicate with clients");
                }
            } else if (permission.getName().equals("Fun admin access")) {
                adminLtBinding.extras.setText("List fun spaces with us and get paid.");
            }

            adminLtBinding.requestAdmin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(permission);
                }
            });
        }
    }
}
