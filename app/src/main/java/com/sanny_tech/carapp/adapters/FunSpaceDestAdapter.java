package com.sanny_tech.carapp.adapters;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.activities.AboutCarActivity;
import com.sanny_tech.carapp.databinding.DestinationLtBinding;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.entities.FunSpace;
import com.sanny_tech.carapp.fun_utils.AboutFunSpaceActivity;
import com.sanny_tech.carapp.fun_utils.LikeManager;
import com.sanny_tech.carapp.fun_utils.SpaceDest;
import com.sanny_tech.carapp.utils.IpAddressManager;

import java.util.ArrayList;
import java.util.List;

public class FunSpaceDestAdapter extends RecyclerView.Adapter<FunSpaceDestAdapter.ViewHolder> {

    private Context context;
    private List<SpaceDest> funSpaces;
    private String baseUrl;
    private List<String> hiredCars = new ArrayList<>();
    private LikeManager likeManager;
    private boolean isLikedMain = false;

    public FunSpaceDestAdapter(Context context, List<SpaceDest> funSpaces) {
        this.context = context;
        this.funSpaces = funSpaces;
        this.baseUrl = IpAddressManager.getIpAddress(context);
        this.likeManager = new LikeManager();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        DestinationLtBinding destinationLtBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.destination_lt, parent, false);
        return new ViewHolder(destinationLtBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SpaceDest spaceDest = funSpaces.get(position);

        holder.bind(spaceDest);
    }

    @Override
    public int getItemCount() {
        return funSpaces.size();
    }

    public void setItems(List<SpaceDest> funSpaceList) {
        funSpaces.clear();
        funSpaces.addAll(funSpaceList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private DestinationLtBinding destinationLtBinding;

        public ViewHolder(@NonNull DestinationLtBinding destinationLtBinding) {
            super(destinationLtBinding.getRoot());
            this.destinationLtBinding = destinationLtBinding;
        }

        void bind(SpaceDest spaceDest) {
            if (spaceDest.getImages_urls() != null) {
                glideImage(spaceDest, destinationLtBinding.imageView);
            }

            destinationLtBinding.title.setText(spaceDest.getName());
            destinationLtBinding.title2.setText(spaceDest.getOwner_name());
            if (spaceDest.getLocation() != null) {
                destinationLtBinding.title3.setText(spaceDest.getLocation().getName());
            }
            destinationLtBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAboutSpace(spaceDest);
                }
            });
        }

        private void glideImage(SpaceDest funSpace, ImageView imageView) {
            if (funSpace != null) {
                Glide.with(context).asBitmap().load(funSpace.getImages_urls().get(0))
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

    private void openAboutSpace(SpaceDest spaceDest) {
        Intent intent = new Intent(context, AboutFunSpaceActivity.class);
        intent.putExtra("space", spaceDest);
        intent.putExtra("instruction", "local");
        context.startActivity(intent);
    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AccountPrefs",
                MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
}

