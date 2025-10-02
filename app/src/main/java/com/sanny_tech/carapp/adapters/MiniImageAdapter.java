package com.sanny_tech.carapp.adapters;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.databinding.MiniImageBinding;
import com.sanny_tech.carapp.utils.IpAddressManager;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MiniImageAdapter extends RecyclerView.Adapter<MiniImageAdapter.MiniImageViewHolder> {
    private List<String> images;
    private Context context;
    private OnItemClickListener listener;
    private OnCancelClickListener cancelClickListener;
    private String baseUrl;

    public MiniImageAdapter(List<String> images, Context context) {
        this.images = images;
        this.context = context;
        this.baseUrl = IpAddressManager.getIpAddress(context) + "/";
    }

    @NonNull
    @Override
    public MiniImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        MiniImageBinding miniImageBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.mini_image,parent,false);
        return new MiniImageViewHolder(miniImageBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MiniImageViewHolder holder, int position) {
        String image = images.get(position);

        holder.bind(image);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void removeItem(String item) {
        images.remove(item);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(String item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public interface OnCancelClickListener {
        void onCancelClick(String item);
    }

    public void setOnCancelClickListener(OnCancelClickListener cancelClickListener) {
        this.cancelClickListener = cancelClickListener;
    }

    public class MiniImageViewHolder extends RecyclerView.ViewHolder{
        private MiniImageBinding miniImageBinding;

        public MiniImageViewHolder(@NonNull MiniImageBinding miniImageBinding) {
            super(miniImageBinding.getRoot());
            this.miniImageBinding = miniImageBinding;
        }
        public void bind(String image) {
//            miniImageBinding.closeButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    cancelClickListener.onCancelClick(image);
//                }
//            });
            miniImageBinding.subImage1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(image);
                }
            });
            String endPoint = baseUrl + "taxi/image/" + getCurrentAccountId() + "/"
                    + image;
//            Glide.with(context)
//                    .load(endPoint)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache all versions of the image
//                    .skipMemoryCache(true)
//                    .into(miniImageBinding.subImage1);
            Picasso.get()
                    .load(endPoint)
                    .into(miniImageBinding.subImage1);

        }
    }
    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
}
