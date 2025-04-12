package com.sanny_tech.carapp.adapters;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
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
import com.sanny_tech.carapp.databinding.FunItemBinding;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.entities.FunSpace;
import com.sanny_tech.carapp.fun_utils.LikeManager;
import com.sanny_tech.carapp.utils.IpAddressManager;

import java.util.ArrayList;
import java.util.List;

public class FunSpacesAdapter extends RecyclerView.Adapter<FunSpacesAdapter.ViewHolder> {

    private Context context;
    private List<FunSpace> funSpaces;
    private String baseUrl;
    private List<String> hiredCars = new ArrayList<>();
    private LikeManager likeManager;
    private boolean isLikedMain = false;

    public FunSpacesAdapter(Context context, List<FunSpace> funSpaces) {
        this.context = context;
        this.funSpaces = funSpaces;
        this.baseUrl = IpAddressManager.getIpAddress(context);
        this.likeManager = new LikeManager();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        FunItemBinding funItemBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.fun_item, parent, false);
        return new ViewHolder(funItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FunSpace funSpace = funSpaces.get(position);

        holder.bind(funSpace);
    }

    @Override
    public int getItemCount() {
        return funSpaces.size();
    }

    public void setItems(List<FunSpace> funSpaceList) {
        funSpaces.clear();
        funSpaces.addAll(funSpaceList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private FunItemBinding funItemBinding;

        public ViewHolder(@NonNull FunItemBinding funItemBinding) {
            super(funItemBinding.getRoot());
            this.funItemBinding = funItemBinding;
        }

        void bind(FunSpace funSpace) {
            if (funSpace.getImages() != null) {
                glideImage(funSpace, funItemBinding.imageView);
            }

            funItemBinding.description.setText(funSpace.getDesc());

            if (funSpace.getDestination() != null) {
                funItemBinding.destName.setText(funSpace.getDestination().getName());
                Glide.with(context).asBitmap().load(funSpace.getDestination().getImages_urls().get(0))
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.baseline_downloading_350) // Placeholder image while loading
                                .error(R.drawable.baseline_downloading_350)      // Error image if loading fails
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .override(ViewGroup.LayoutParams.MATCH_PARENT, 32)
                        .into(funItemBinding.logo);
            }
            funItemBinding.likesCount.setText(String.valueOf(funSpace.getLikes()));
            funItemBinding.dislikesCount.setText(String.valueOf(funSpace.getDislikes()));

            loadLikeStatus(funSpace);
            funItemBinding.like.setOnClickListener(v -> {
                int newLikes;
                if (isLikedMain) {
                    newLikes = funSpace.getLikes() - 1;
                } else {
                    newLikes = funSpace.getLikes() + 1;
                }
                funSpace.setLikes(newLikes);
                updateLikesInFirebase(funSpace.getId(), newLikes);
                funItemBinding.likesCount.setText(String.valueOf(newLikes));
                loadLikeStatus(funSpace);
            });

            funItemBinding.dislike.setOnClickListener(v -> {
                int newDislikes;
                if (!isLikedMain){
                    newDislikes = funSpace.getDislikes() - 1;
                }else {
                    newDislikes = funSpace.getDislikes() + 1;
                }
                funSpace.setDislikes(newDislikes);
                updateDislikesInFirebase(funSpace.getId(), newDislikes);
                funItemBinding.dislikesCount.setText(String.valueOf(newDislikes));
                loadLikeStatus(funSpace);
            });
        }

        private void loadLikeStatus(FunSpace funSpace) {
            likeManager.checkLikeStatus(getCurrentAccountId(), funSpace.getId(), new LikeManager.LikeStatusCallback() {
                @Override
                public void onLikeStatusChecked(boolean isAdmin) {
                    if (isAdmin) {
                        isLikedMain = true;
                        funItemBinding.like.setImageResource(R.drawable.thumb_like_filled_icon);
                        funItemBinding.dislike.setImageResource(R.drawable.fluent_thumb_dislike_icon);
                    } else {
                        isLikedMain = false;
                        funItemBinding.dislike.setImageResource(R.drawable.thumb_dislike_filled_icon);
                        funItemBinding.like.setImageResource(R.drawable.thumb_like_icon);
                    }
                }
            });
        }

        private void glideImage(FunSpace funSpace, ImageView imageView) {
            if (funSpace != null) {
                Glide.with(context).asBitmap().load(funSpace.getImages().get(0))
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
        intent.putExtra("selectedCar", car);
        intent.putExtra("instruction", "local");
        context.startActivity(intent);
    }

    private void updateLikesInFirebase(String id, int newLikes) {
        if (!isLikedMain) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("fun_spaces").child(id);
            databaseReference.child("likes").setValue(newLikes);
            likeManager.likeFunItem(getCurrentAccountId(), id, true);
        }
    }

    private void updateDislikesInFirebase(String id, int newDislikes) {
        if (isLikedMain) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("fun_spaces").child(id);
            databaseReference.child("dislikes").setValue(newDislikes);
            likeManager.likeFunItem(getCurrentAccountId(), id, false);
        }
    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AccountPrefs",
                MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
}

