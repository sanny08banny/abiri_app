package com.sanny_tech.carapp.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.MessageAdapter;
import com.sanny_tech.carapp.databinding.ActivityNotificationsBinding;
import com.sanny_tech.carapp.dialogs.BookingBottomSheet;
import com.sanny_tech.carapp.entities.CarBookRequest;
import com.sanny_tech.carapp.entities.Message;
import com.sanny_tech.carapp.entities.TaxiLocation;
import com.sanny_tech.carapp.storage.RemoteMessageSaver;
import com.sanny_tech.carapp.taxi_utils.ClientRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotificationsActivity extends AppCompatActivity implements MessageAdapter.OnItemClickListener,
MessageAdapter.SelectionStateListener, BookingBottomSheet.TaxiBookingListener{
    private ActivityNotificationsBinding notificationsBinding;
    private MessageAdapter messageAdapter;
    private BookingBottomSheet bookingBottomSheet;
    private Map<String, String> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationsBinding = DataBindingUtil.setContentView(this,R.layout.activity_notifications);

        setSupportActionBar(notificationsBinding.toolbar);
        notificationsBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        messageAdapter = new MessageAdapter(fetchMessages(this),this);
        messageAdapter.setOnItemClickListener(this);
        messageAdapter.setSelectionStateListener(this);
        notificationsBinding.notifications.setAdapter(messageAdapter);
        notificationsBinding.notifications.setLayoutManager(new LinearLayoutManager(this));

        notificationsBinding.toggleSelectionButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        messageAdapter.clearSelection();
                    }
                }
        );
    }
    public List<Message> fetchMessages(Context context) {
        List<Message> messageList = new ArrayList<>();
        try {
            JSONArray messagesArray = RemoteMessageSaver.getAllMessages(context);
            for (int i = 0; i < messagesArray.length(); i++) {
                JSONObject messageObject = messagesArray.getJSONObject(i);
                long id = messageObject.getLong("id");
                String data = messageObject.getJSONObject("data").toString();
                boolean isRead = messageObject.getBoolean("isRead");

                Message message = new Message(id, data, isRead);
                messageList.add(message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
        return messageList;
    }

    @Override
    public void onItemClick(Message item) {
        data = jsonStringToMap(item.getData());
        Object object = processData(data);
        if (object instanceof ClientRequest){
            openDriverMaps((ClientRequest) object, item);
        }else if (object instanceof CarBookRequest){
            showBookingWindow((CarBookRequest) object, item);
        }
    }
    private void showBookingWindow(CarBookRequest bookingRequest, Message item) {
        if (!item.isRead()){
            try {
                RemoteMessageSaver.readMessageById(this,item.getId());
                messageAdapter.changeReadState(true,item);
            } catch (JSONException e) {
                Log.e("Main activity", String.valueOf(e));
                throw new RuntimeException(e);
            }
    }
        bookingBottomSheet = new BookingBottomSheet(bookingRequest);
        bookingBottomSheet.setBookingListener(this);
        bookingBottomSheet.show(getSupportFragmentManager(), bookingBottomSheet.getTag());
    }
    private void openDriverMaps(ClientRequest request, Message item) {
        if (!item.isRead()){
            try {
                RemoteMessageSaver.readMessageById(this,item.getId());
                messageAdapter.changeReadState(true,item);
            } catch (JSONException e) {
                Log.e("Main activity", String.valueOf(e));
                throw new RuntimeException(e);
            }
        }
        Intent intent = new Intent(this, TaxiMapsActivity.class);
        intent.putExtra("request", request);
        startActivity(intent);
    }

    @Override
    public void onSelectionModeChanged(boolean isInSelectionMode) {
        notificationsBinding.buttonsLt.setVisibility(isInSelectionMode ? View.GONE : View.VISIBLE);
        notificationsBinding.selectionControls.setVisibility(isInSelectionMode ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onSelectedItemsChanged(Set<Integer> selectedItems) {

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
            String car_id = data.get("car_id");
            String user_phone = data.get("user_phone");
            String client_id = data.get("client_id");
            CarBookRequest request = new CarBookRequest();
            request.setClient_id(client_id);
            request.setUser_name(user_name);
            request.setUser_phone(user_phone);
            request.setCar_id(car_id);
            request.setBooking_id(booking_id);
            return request;
        }
        return null;
    }

    @Override
    public void onBookingResponse(boolean isSuccess, TaxiLocation item) {

    }
}