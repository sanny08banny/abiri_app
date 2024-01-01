package com.sanny_tech.carapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.databinding.PreviewImageBinding;

import java.util.List;

public class PreviewImageAdapter extends RecyclerView.Adapter<PreviewImageAdapter.PreviewImageViewHolder> {
    private List<String> images;
    private Context context;
    private OnItemClickListener listener;

    public PreviewImageAdapter(List<String> images, Context context) {
        this.images = images;
        this.context = context;
    }

    @NonNull
    @Override
    public PreviewImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        PreviewImageBinding previewImageBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.preview_image,parent,false);
        return new PreviewImageViewHolder(previewImageBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PreviewImageViewHolder holder, int position) {
        String image = images.get(position);

        holder.bind(image);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public interface OnItemClickListener {
        void onItemClick(String item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class PreviewImageViewHolder extends RecyclerView.ViewHolder{
        private PreviewImageBinding previewImageBinding;

        public PreviewImageViewHolder(@NonNull PreviewImageBinding previewImageBinding) {
            super(previewImageBinding.getRoot());
            this.previewImageBinding = previewImageBinding;
        }
        public void bind(String image) {
            Glide.with(context)
                    .asBitmap()
                    .load(image)
                    .into(previewImageBinding.previewImage);
        }
    }
}
