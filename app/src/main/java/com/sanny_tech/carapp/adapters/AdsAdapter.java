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
import com.sanny_tech.carapp.databinding.MiniImageBinding;
import com.sanny_tech.carapp.entities.Icon;

import java.util.List;

public class AdsAdapter extends RecyclerView.Adapter<AdsAdapter.AdViewHolder> {

    private Context context;
    private List<Icon> icons;
    private String baseUrl;
    private OnItemClickListener listener;

    public AdsAdapter(Context context, List<Icon> icons) {
        this.context = context;
        this.icons = icons;
        this.baseUrl = context.getResources().getString(R.string.base_url_title);

    }

    @NonNull
    @Override
    public AdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        MiniImageBinding miniImageBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.mini_image,parent,false);
        return new AdViewHolder(miniImageBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdViewHolder holder, int position) {
        Icon icon = icons.get(position);

        holder.bind(icon);
    }

    @Override
    public int getItemCount() {
        return icons.size();
    }

    public void setItems(List<Icon> data) {
        icons.clear();
        icons.addAll(data);
        notifyDataSetChanged();
    }
    public interface OnItemClickListener {
        void onItemClick(Icon item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class AdViewHolder extends RecyclerView.ViewHolder {
        private MiniImageBinding miniImageBinding;

        public AdViewHolder(@NonNull MiniImageBinding miniImageBinding) {
            super(miniImageBinding.getRoot());
            this.miniImageBinding = miniImageBinding;
        }
        void bind(Icon icon){
                glideImage(icon.getImage(), miniImageBinding.subImage1);
                miniImageBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(icon);
                    }
                });
        }
    }

    private void glideImage(int url, ImageView imageView) {
        if (url != 0) {
            String endPoint = baseUrl + "/";
            Glide.with(context)
                    .load(url)
                    .into(imageView);
        }
    }
}

