package com.sanny_tech.carapp.adapters;

// MyAdapter.java
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.databinding.OptionItemsLtBinding;
import com.sanny_tech.carapp.entities.OptionItem;

import java.util.List;

public class OptionItemAdapter extends RecyclerView.Adapter<OptionItemAdapter.ViewHolder> {

    private List<OptionItem> itemList;
    private int selectedPosition = -1;
    private OnItemClickListener listener;

    public OptionItemAdapter(List<OptionItem> itemList) {
        this.itemList = itemList;
    }
    public interface OnItemClickListener {
        void onItemClick(OptionItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        OptionItemsLtBinding optionItemsLtBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.option_items_lt, parent, false);
        return new ViewHolder(optionItemsLtBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OptionItem item = itemList.get(position);
        holder.title.setText(item.getTitle());
        holder.miniTitle.setText(item.getMiniTitle());
        holder.radioButton.setChecked(position == selectedPosition);

        holder.itemView.setBackgroundResource(position == selectedPosition ? R.drawable.item_background_selected : R.drawable.item_background_default);

        holder.itemView.setOnClickListener(v -> {
            int previousSelectedPosition = selectedPosition;
            selectedPosition = holder.getBindingAdapterPosition();
            notifyItemChanged(previousSelectedPosition);
            notifyItemChanged(selectedPosition);
            listener.onItemClick(item);
        });

        holder.radioButton.setOnClickListener(v -> {
            int previousSelectedPosition = selectedPosition;
            selectedPosition = holder.getBindingAdapterPosition();
            notifyItemChanged(previousSelectedPosition);
            notifyItemChanged(selectedPosition);
            listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView miniTitle;
        public RadioButton radioButton;
        private OptionItemsLtBinding binding;

        public ViewHolder(OptionItemsLtBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            title = binding.title;
            miniTitle = binding.miniTitle;
            radioButton = binding.radioButton;
        }
    }
}

