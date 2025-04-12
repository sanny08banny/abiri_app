package com.sanny_tech.carapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.sanny_tech.carapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {

    private Context context;
    private ArrayList<String> imageResources;
    private String baseUrl;

    public ImagePagerAdapter(Context context, ArrayList<String> imageResources) {
        this.context = context;
        this.imageResources = imageResources;
        this.baseUrl = context.getResources().getString(R.string.base_url_title);
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String resourceId = imageResources.get(position);
        Picasso.get()
                .load(resourceId)
                .into(holder.imageView);
//        Glide.with(context)
//                .asBitmap() // Ensure Glide loads the image as a Bitmap
//                .load(resourceId)
//                .apply(new RequestOptions()
//                        .placeholder(R.drawable.baseline_downloading_350)
//                        .error(R.drawable.baseline_downloading_350)
//                        .diskCacheStrategy(DiskCacheStrategy.ALL))
//                .into(new CustomTarget<Bitmap>() {
//                    @Override
//                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                        // Set the loaded bitmap to the ImageView
//                        holder.imageView.setImageBitmap(resource);
//
//                        // Retain the original aspect ratio of the image
//                        float aspectRatio = (float) resource.getWidth() / resource.getHeight();
//                        int desiredHeight = (int) (holder.imageView.getWidth() / aspectRatio);
//                        ViewGroup.LayoutParams layoutParams = holder.imageView.getLayoutParams();
//                        layoutParams.height = desiredHeight;
//                        holder.imageView.setLayoutParams(layoutParams);
//                    }
//
//                    @Override
//                    public void onLoadCleared(@Nullable Drawable placeholder) {
//                        // Handle resource cleanup if needed
//                    }
//                });
    }

    @Override
    public int getItemCount() {
        return imageResources.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}

