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
import com.sanny_tech.carapp.databinding.OptionItemLt2Binding;
import com.sanny_tech.carapp.databinding.OptionItemsLtBinding;
import com.sanny_tech.carapp.entities.OptionItem;

import java.util.List;

public class OptionItem2Adapter extends RecyclerView.Adapter<OptionItem2Adapter.ViewHolder> {

    private List<OptionItem> itemList;
    private int selectedPosition = -1;
    private OnItemClickListener listener;

    public OptionItem2Adapter(List<OptionItem> itemList) {
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
        OptionItemLt2Binding optionItemsLtBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.option_item_lt2, parent, false);
        return new ViewHolder(optionItemsLtBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OptionItem item = itemList.get(position);
        holder.title.setText(item.getTitle());
        holder.miniTitle.setText(item.getMiniTitle());
        holder.title3.setText(item.getTitle3());

        holder.itemView.setBackgroundResource(position == selectedPosition ? R.drawable.item_background_selected : R.drawable.item_background_default);

        holder.itemView.setOnClickListener(v -> {
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
        public TextView title,title3;
        public TextView miniTitle;
        private OptionItemLt2Binding binding;

        public ViewHolder(OptionItemLt2Binding binding) {
            super(binding.getRoot());
            this.binding = binding;
            title = binding.title;
            title3 = binding.title3;
            miniTitle = binding.miniTitle;
        }
    }
}

