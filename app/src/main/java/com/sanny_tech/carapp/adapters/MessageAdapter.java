package com.sanny_tech.carapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.databinding.MessageItemBinding;
import com.sanny_tech.carapp.entities.CarBookRequest;
import com.sanny_tech.carapp.entities.Message;
import com.sanny_tech.carapp.taxi_utils.ClientRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.TaxiViewHolder> {
    private List<Message> messages;
    private Context context;
    private OnItemClickListener listener;
    private SelectionStateListener selectionStateListener;
    private boolean isSelectionMode = false;
    private Set<Integer> selectedItems = new HashSet<>();

    public MessageAdapter(List<Message> messages, Context context) {
        this.messages = messages;
        this.context = context;
        sortMessages();
    }

    @NonNull
    @Override
    public TaxiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        MessageItemBinding messageItemBinding = DataBindingUtil.inflate(layoutInflater,
                R.layout.message_item, parent, false);
        return new TaxiViewHolder(messageItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaxiViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.bind(message, selectedItems.contains(position));

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(position);
            } else {
                listener.onItemClick(message);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                isSelectionMode = true;
                toggleSelection(position);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private void toggleSelection(int position) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position);
        } else {
            selectedItems.add(position);
        }
        notifyItemChanged(position);

        if (selectedItems.isEmpty() && isSelectionMode) {
            isSelectionMode = false;
            if (selectionStateListener != null) {
                selectionStateListener.onSelectionModeChanged(false);
            }
        }if (!selectedItems.isEmpty() && isSelectionMode) {
            if (selectionStateListener != null) {
                selectionStateListener.onSelectionModeChanged(true);
            }
        } else if (!isSelectionMode) {
            if (selectionStateListener != null) {
                selectionStateListener.onSelectionModeChanged(false);
            }
        }

        if (selectionStateListener != null) {
            selectionStateListener.onSelectedItemsChanged(new HashSet<>(selectedItems));
        }
    }

    public void clearSelection() {
        selectedItems.clear();
        isSelectionMode = false;
        selectionStateListener.onSelectionModeChanged(isSelectionMode);
        notifyDataSetChanged();
    }

    public void changeReadState(boolean b, Message item) {
        messages.remove(item);
        item.setRead(b);
        messages.add(item);
        sortMessages();
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(Message item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public interface SelectionStateListener {
        void onSelectionModeChanged(boolean isInSelectionMode);
        void onSelectedItemsChanged(Set<Integer> selectedItems);
    }
    public void setSelectionStateListener(SelectionStateListener listener) {
        this.selectionStateListener = listener;
    }

    public class TaxiViewHolder extends RecyclerView.ViewHolder {
        private MessageItemBinding messageItemBinding;

        public TaxiViewHolder(@NonNull MessageItemBinding messageItemBinding) {
            super(messageItemBinding.getRoot());
            this.messageItemBinding = messageItemBinding;
        }

        public void bind(Message message, boolean isSelected) {
            // Bind your message data to the view here
            itemView.setBackgroundColor(isSelected ? Color.LTGRAY : Color.TRANSPARENT);
            Map<String, String> map = jsonStringToMap(message.getData());
            Object object = processData(map);
            if (object != null) {
                if (object instanceof ClientRequest) {
                    ClientRequest request = (ClientRequest) object;
                    messageItemBinding.message.setText(MessageFormat.format(
                            "{0} has requested for a ride.", request.getUser_name()));
                    messageItemBinding.searchIcon.setImageResource(R.drawable.baseline_local_taxi_24);
                } else if (object instanceof CarBookRequest) {
                    CarBookRequest request = (CarBookRequest) object;
                    messageItemBinding.message.setText(MessageFormat.format(
                            "{0} has requested for a car hire.", request.getUser_name()));
                    messageItemBinding.searchIcon.setImageResource(R.drawable.baseline_car_rental_24);
                }
                messageItemBinding.time.setText(formatTime(message.getId()));
                boolean hasUnread = !message.isRead();
                messageItemBinding.notificationDot.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
            }
        }
        private String formatTime(Long timestamp) {
            Log.d("date", String.valueOf(timestamp));
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
    private void sortMessages() {
        Collections.sort(messages, new Comparator<Message>() {
            @Override
            public int compare(Message m1, Message m2) {
                return Long.compare(m2.getId(), m1.getId());
            }
        });
    }
    public static Map<String, String> jsonStringToMap(String jsonString) {
        Map<String, String> map = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            Iterator<String> keys = jsonObject.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                String value = jsonObject.getString(key);
                map.put(key, value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // Handle exception
        }

        return map;
    }
    private Object processData(Map<String, String> data) {
        if (data.containsKey("ride_id")) {
            String ride_id = data.get("ride_id");
            String user_name = data.get("user_name");
            String user_phone = data.get("user_phone");
            String client_id = data.get("client_id");
            float dest_lat = Float.parseFloat(data.get("dest_lat"));
            float dest_lon = Float.parseFloat(data.get("dest_lon"));
            float current_lat = Float.parseFloat(data.get("current_lat"));
            float current_lon = Float.parseFloat(data.get("current_lon"));

            ClientRequest request = new ClientRequest();
            request.setRide_id(ride_id);
            request.setClient_id(client_id);
            request.setUser_name(user_name);
            request.setUser_phone(user_phone);
            request.setCurrent_lat(current_lat);
            request.setCurrent_lon(current_lon);
            request.setDest_lat(dest_lat);
            request.setDest_lon(dest_lon);
            return request;
        }else if (data.containsKey("booking_id")){
            String booking_id = data.get("booking_id");
            String user_name = data.get("user_name");
            String user_phone = data.get("user_phone");
            String client_id = data.get("client_id");
            CarBookRequest request = new CarBookRequest();
            request.setClient_id(client_id);
            request.setUser_name(user_name);
            request.setUser_phone(user_phone);
            request.setBooking_id(booking_id);
            return request;
        }
        return null;
    }

}
