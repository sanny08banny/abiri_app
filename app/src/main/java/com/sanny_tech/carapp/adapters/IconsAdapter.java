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
import com.sanny_tech.carapp.databinding.IconItemBinding;
import com.sanny_tech.carapp.entities.Icon;

import java.util.List;

public class IconsAdapter extends RecyclerView.Adapter<IconsAdapter.IconViewHolder> {

    private Context context;
    private List<Icon> icons;
    private String baseUrl;
    private OnItemClickListener listener;

    public IconsAdapter(Context context, List<Icon> icons) {
        this.context = context;
        this.icons = icons;
        this.baseUrl = context.getResources().getString(R.string.base_url_title);

    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        IconItemBinding iconItemBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.icon_item,parent,false);
        return new IconViewHolder(iconItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
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

    public class IconViewHolder extends RecyclerView.ViewHolder {
        private IconItemBinding iconItemBinding;

        public IconViewHolder(@NonNull IconItemBinding iconItemBinding) {
            super(iconItemBinding.getRoot());
            this.iconItemBinding = iconItemBinding;
        }
        void bind(Icon icon){
                glideImage(icon.getImage(), iconItemBinding.iconImageView);
                iconItemBinding.desc.setText(icon.getDesc());

                iconItemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
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

