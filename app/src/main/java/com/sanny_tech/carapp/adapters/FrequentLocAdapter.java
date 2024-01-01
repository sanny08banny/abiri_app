package com.sanny_tech.carapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sanny_tech.carapp.R;

import java.util.List;

public class FrequentLocAdapter extends ArrayAdapter<String> {
    private LayoutInflater inflater;

    public FrequentLocAdapter(Context context) {
        super(context, 0);
        inflater = LayoutInflater.from(context);
    }

    public void setAddressItems(List<String> addressItems) {
        clear();
        addAll(addressItems);
    }
    public void addAddressItem(String addressItem) {
        add(addressItem);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_address, parent, false);

            holder = new ViewHolder();
            holder.addressText = convertView.findViewById(R.id.address_text);


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String item = getItem(position);
        if (item != null) {
            holder.addressText.setText(item);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView addressText;
        TextView distanceText;
    }
}

