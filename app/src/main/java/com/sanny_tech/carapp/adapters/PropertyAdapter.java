package com.sanny_tech.carapp.adapters;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.databinding.PropertyItemBinding;
import com.sanny_tech.carapp.entities.OptionItem;
import com.sanny_tech.carapp.utils.IpAddressManager;

import java.util.List;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.TaxiViewHolder> {
    private List<OptionItem> optionItems;
    private Context context;
    private OnItemClickListener listener;
    private String baseUrl;
    private int selectedPosition = -1;

    public PropertyAdapter(List<OptionItem> optionItems, Context context) {
        this.optionItems = optionItems;
        this.context = context;
        this.baseUrl = IpAddressManager.getIpAddress(context);
    }

    public void setItems(List<OptionItem> data) {
        optionItems.clear();
        optionItems.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaxiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        PropertyItemBinding propertyItemBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.property_item, parent, false);
        return new TaxiViewHolder(propertyItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaxiViewHolder holder, int position) {
        OptionItem optionItem = optionItems.get(position);

//        holder.itemView.setOnClickListener(v -> {
//            int previousSelectedPosition = selectedPosition;
//            selectedPosition = holder.getBindingAdapterPosition();
//            notifyItemChanged(previousSelectedPosition);
//            notifyItemChanged(selectedPosition);
//            listener.onItemClick(optionItem);
//        });
        holder.bind(optionItem,position);
    }

    @Override
    public int getItemCount() {
        return optionItems.size();
    }

    public interface OnItemClickListener {
        void onItemClick(OptionItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class TaxiViewHolder extends RecyclerView.ViewHolder {
        private PropertyItemBinding propertyItemBinding;

        public TaxiViewHolder(@NonNull PropertyItemBinding propertyItemBinding) {
            super(propertyItemBinding.getRoot());
            this.propertyItemBinding = propertyItemBinding;
        }

        public void bind(OptionItem optionItem, int position) {
            if (optionItem.getTitle().equals("Working Hours")){
                propertyItemBinding.image.setImageResource(R.drawable.access_time_24px);
                propertyItemBinding.taxiDesc.setText(optionItem.getTitle());
                String formattedHours = optionItem.getMiniTitle();
                String fullText = "This business is open between " + formattedHours;
                SpannableString spannableString = new SpannableString(fullText);
                int start = fullText.indexOf(formattedHours);
                int end = start + formattedHours.length();
                spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                propertyItemBinding.seatCount.setText(spannableString);
            }else {
                propertyItemBinding.image.setImageResource(R.drawable.spa);
                if (optionItem.getTitle().equals("Family")) {
                    propertyItemBinding.taxiDesc.setText("Family and Kids");
                }else {
                    propertyItemBinding.taxiDesc.setText("Adults and Groups");
                }
                String owner = optionItem.getMiniTitle();
                String fullText = "Owned by " + owner;
                SpannableString spannableString = new SpannableString(fullText);
                int start = fullText.indexOf(owner);
                int end = start + owner.length();
                spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                propertyItemBinding.seatCount.setText(spannableString);
            }
        }
    }
}
