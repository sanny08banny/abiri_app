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
import com.sanny_tech.carapp.databinding.DocumentItemBinding;
import com.sanny_tech.carapp.entities.Icon;
import com.sanny_tech.carapp.utils.IpAddressManager;

import java.util.ArrayList;
import java.util.List;

public class DriverDocsAdapter extends RecyclerView.Adapter<DriverDocsAdapter.DocsViewHolder> {
    private List<String> docs;
    private List<Icon> allDocs;
    private List<String> unverifiedDocs;
    private Context context;
    private OnItemClickListener listener;
    private String baseUrl;
    private int selectedPosition = -1;
    private String driver_id;

    public DriverDocsAdapter(List<String> docs, Context context, String driverId) {
        List<Icon> docs1 = new ArrayList<>();
        this.allDocs = docs1;
        this.docs = docs;
        this.unverifiedDocs = new ArrayList<>();
        this.context = context;
        this.baseUrl = IpAddressManager.getIpAddress(context) + "/";
        driver_id = driverId;
    }

    public void setItems(List<String> data, ArrayList<String> stringArrayList) {
        List<Icon> docs1 = new ArrayList<>();
        docs1.add(new Icon(R.drawable.id_card, "NationalId"));
        docs1.add(new Icon(R.drawable.icon_car_insurance, "Insurance"));
        docs1.add(new Icon(R.drawable.driving_license, "DrivingLicense"));
        docs1.add(new Icon(R.drawable.approved, "PsvLicense"));
        docs1.add(new Icon(R.drawable.checklist, "InspectionReport"));
        allDocs.clear();
        allDocs.addAll(docs1);
        if (data != null) {
            docs.clear();
            for (String doc : data) {
                String doc1 = convertText(doc);
                docs.add(doc1);
            }
        }
        if (stringArrayList != null) {
            unverifiedDocs.clear();
            for (String doc : stringArrayList) {
                String doc1 = convertText(doc);
                unverifiedDocs.add(doc1);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DocsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        DocumentItemBinding documentItemBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.document_item, parent, false);
        return new DocsViewHolder(documentItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull DocsViewHolder holder, int position) {
        Icon doc = allDocs.get(position);
        holder.bind(doc);
    }

    @Override
    public int getItemCount() {
        return allDocs.size();
    }

    public interface OnItemClickListener {
        void onItemClick(String item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class DocsViewHolder extends RecyclerView.ViewHolder {
        private DocumentItemBinding documentItemBinding;

        public DocsViewHolder(@NonNull DocumentItemBinding documentItemBinding) {
            super(documentItemBinding.getRoot());
            this.documentItemBinding = documentItemBinding;
        }

        public void bind(Icon doc) {
            if (docs.contains(doc.getDesc())) {
                documentItemBinding.status.setText("Pending");
            }else {
                if (unverifiedDocs.contains(doc.getDesc())) {
                    documentItemBinding.status.setVisibility(View.GONE);
                    documentItemBinding.uploadButton.setVisibility(View.VISIBLE);
                }
            }

            documentItemBinding.title.setText(doc.getDesc());
            glideImage(doc.getImage(), documentItemBinding.commentProfileImage);
            documentItemBinding.uploadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(doc.getDesc());
                }
            });
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

    private String convertText(String input) {
        if (input == null || input.isEmpty()) {
            return input; // return the input if it's null or empty
        }

        // Split the input by underscore
        String[] parts = input.split("_");

        // Create a StringBuilder to hold the result
        StringBuilder result = new StringBuilder();

        // Capitalize the first letter of each part and append to the result
        for (String part : parts) {
            if (part.length() > 0) {
                result.append(Character.toUpperCase(part.charAt(0)));
                result.append(part.substring(1));
            }
        }

        return result.toString();
    }
}
