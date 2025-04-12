package com.sanny_tech.carapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.databinding.MiniPreviewImageBinding;
import com.sanny_tech.carapp.databinding.PreviewImageBinding;

import java.util.List;

public class MiniPreviewImageAdapter extends RecyclerView.Adapter<MiniPreviewImageAdapter.MiniPreviewImageViewHolder> {
    private List<String> images;
    private Context context;
    private OnItemClickListener listener;
    private OnCancelClickListener cancelClickListener;

    public MiniPreviewImageAdapter(List<String> images, Context context) {
        this.images = images;
        this.context = context;
    }

    @NonNull
    @Override
    public MiniPreviewImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        MiniPreviewImageBinding previewImageBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.mini_preview_image,parent,false);
        return new MiniPreviewImageViewHolder(previewImageBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MiniPreviewImageViewHolder holder, int position) {
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

    public class MiniPreviewImageViewHolder extends RecyclerView.ViewHolder{
        private MiniPreviewImageBinding previewImageBinding;

        public MiniPreviewImageViewHolder(@NonNull MiniPreviewImageBinding previewImageBinding) {
            super(previewImageBinding.getRoot());
            this.previewImageBinding = previewImageBinding;
        }
        public void bind(String image) {
            previewImageBinding.closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelClickListener.onCancelClick(image);
                }
            });
            previewImageBinding.subImage1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(image);
                }
            });
            Glide.with(context)
                    .asBitmap()
                    .load(image)
                    .into(previewImageBinding.subImage1);
        }
    }
}
