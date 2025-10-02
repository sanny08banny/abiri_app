package com.sanny_tech.carapp.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.databinding.ChipItemBinding;

import java.util.List;

public class ChipAdapter extends RecyclerView.Adapter<ChipAdapter.ChipViewHolder> {

    private final List<String> chipList;

    public ChipAdapter(List<String> chipList) {
        this.chipList = chipList;
    }

    @NonNull
    @Override
    public ChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ChipItemBinding chipItemBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.chip_item, parent, false);
        return new ChipViewHolder(chipItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChipViewHolder holder, int position) {
        String chipText = chipList.get(position);
        holder.bind(chipText);
    }

    @Override
    public int getItemCount() {
        return chipList.size();
    }

    static class ChipViewHolder extends RecyclerView.ViewHolder {
        private ChipItemBinding chipItemBinding;
        ChipViewHolder(@NonNull ChipItemBinding chipItemBinding) {
            super(chipItemBinding.getRoot());
            this.chipItemBinding = chipItemBinding;
        }

        public void bind(String chipText) {
            chipItemBinding.chipText.setText(chipText);
        }
    }
}
