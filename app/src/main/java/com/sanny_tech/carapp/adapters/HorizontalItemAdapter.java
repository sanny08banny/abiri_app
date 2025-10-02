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
import com.sanny_tech.carapp.databinding.HorizontalItemBinding;
import com.sanny_tech.carapp.entities.Icon;
import com.sanny_tech.carapp.entities.OptionItem;

import java.util.List;

public class HorizontalItemAdapter extends RecyclerView.Adapter<HorizontalItemAdapter.HorizontalItemViewHolder> {

    private Context context;
    private List<OptionItem> optionItems;
    private String baseUrl;
    private OnItemClickListener listener;

    public HorizontalItemAdapter(Context context, List<OptionItem> optionItems) {
        this.context = context;
        this.optionItems = optionItems;
        this.baseUrl = context.getResources().getString(R.string.base_url_title);

    }

    @NonNull
    @Override
    public HorizontalItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        HorizontalItemBinding horizontalItemBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.horizontal_item,parent,false);
        return new HorizontalItemViewHolder(horizontalItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull HorizontalItemViewHolder holder, int position) {
        OptionItem optionItem = optionItems.get(position);

        holder.bind(optionItem);
    }

    @Override
    public int getItemCount() {
        return optionItems.size();
    }

    public void setItems(List<OptionItem> data) {
        optionItems.clear();
        optionItems.addAll(data);
        notifyDataSetChanged();
    }
    public interface OnItemClickListener {
        void onItemClick(OptionItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class HorizontalItemViewHolder extends RecyclerView.ViewHolder {
        private HorizontalItemBinding horizontalItemBinding;

        public HorizontalItemViewHolder(@NonNull HorizontalItemBinding horizontalItemBinding) {
            super(horizontalItemBinding.getRoot());
            this.horizontalItemBinding = horizontalItemBinding;
        }
        void bind(OptionItem optionItem){
                horizontalItemBinding.desc.setText(optionItem.getTitle());
                if (optionItem.getTitle().equals("Settings")){
                    horizontalItemBinding.icon.setImageResource(R.drawable.settings_icon);
                }else if (optionItem.getTitle().equals("Become a service provider and earn")){
                    horizontalItemBinding.icon.setImageResource(R.drawable.baseline_directions_car_24);
                }

                horizontalItemBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(optionItem);
                    }
                });
        }
    }
}

