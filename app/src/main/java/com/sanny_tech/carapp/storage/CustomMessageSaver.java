package com.sanny_tech.carapp.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class CustomMessageSaver {

    private static final String PREF_NAME = "RemoteMessages";
    private static final String MESSAGES_KEY = "messages";

    // Save a new message as unread
    public static long saveRemoteMessage(Context context, Map<String, String> data) throws JSONException {
        if (data != null && !data.isEmpty()) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String existingMessagesJson = sharedPreferences.getString(MESSAGES_KEY, "[]");
            JSONArray messagesArray = new JSONArray(existingMessagesJson);

            long id = System.currentTimeMillis();
            JSONObject messageObject = new JSONObject();
            messageObject.put("id", id); // Assuming unique ID generation via timestamp
            messageObject.put("data", new JSONObject(data));
            messageObject.put("isRead", false);

            messagesArray.put(messageObject);

            sharedPreferences.edit().putString(MESSAGES_KEY, messagesArray.toString()).apply();
            return id;
        }
        return 0;
    }

    // Mark a message as read
    private static void readMessage(Context context, JSONObject messageData) throws JSONException {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String existingMessagesJson = sharedPreferences.getString(MESSAGES_KEY, "[]");
        JSONArray messagesArray = new JSONArray(existingMessagesJson);
        JSONArray updatedMessagesArray = new JSONArray();

        for (int i = 0; i < messagesArray.length(); i++) {
            JSONObject messageObject = messagesArray.getJSONObject(i);
            JSONObject storedMessageData = messageObject.getJSONObject("data");

            if (storedMessageData.toString().equals(messageData.toString())) {
                messageObject.put("isRead", true);
            }
            updatedMessagesArray.put(messageObject);
        }

        sharedPreferences.edit().putString(MESSAGES_KEY, updatedMessagesArray.toString()).apply();
    }

    // Public method to mark a message as read using message data
    public static void markMessageAsRead(Context context, Map<String, String> data) throws JSONException {
        JSONObject messageData = new JSONObject(data);
        readMessage(context, messageData);
        Log.d("Remote messages", "message read");
    }

    // Delete a message
    public static void deleteMessage(Context context, long messageId) throws JSONException {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String existingMessagesJson = sharedPreferences.getString(MESSAGES_KEY, "[]");
        JSONArray messagesArray = new JSONArray(existingMessagesJson);
        JSONArray updatedMessagesArray = new JSONArray();

        for (int i = 0; i < messagesArray.length(); i++) {
            JSONObject messageObject = messagesArray.getJSONObject(i);
            if (messageObject.getLong("id") != messageId) {
                updatedMessagesArray.put(messageObject);
            }
        }

        sharedPreferences.edit().putString(MESSAGES_KEY, updatedMessagesArray.toString()).apply();
    }

    // Retrieve all messages
    public static JSONArray getAllMessages(Context context) throws JSONException {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String existingMessagesJson = sharedPreferences.getString(MESSAGES_KEY, "[]");
        return new JSONArray(existingMessagesJson);
    }

    // Helper method to update the read/unread state of a message
    private static void updateMessageState(Context context, long messageId, boolean isRead) throws JSONException {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String existingMessagesJson = sharedPreferences.getString(MESSAGES_KEY, "[]");
        JSONArray messagesArray = new JSONArray(existingMessagesJson);
        JSONArray updatedMessagesArray = new JSONArray();

        for (int i = 0; i < messagesArray.length(); i++) {
            JSONObject messageObject = messagesArray.getJSONObject(i);
            if (messageObject.getLong("id") == messageId) {
                messageObject.put("isRead", isRead);
            }
            updatedMessagesArray.put(messageObject);
        }

        sharedPreferences.edit().putString(MESSAGES_KEY, updatedMessagesArray.toString()).apply();
    }

    // Method to mark a message as read by ID
    public static void readMessageById(Context context, long id) throws JSONException {
        updateMessageState(context, id, true);
    }

    // Check if there are unread messages
    public static boolean hasUnreadMessages(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String existingMessagesJson = sharedPreferences.getString(MESSAGES_KEY, "[]");
        try {
            JSONArray messagesArray = new JSONArray(existingMessagesJson);
            for (int i = 0; i < messagesArray.length(); i++) {
                JSONObject messageObject = messagesArray.getJSONObject(i);
                boolean isRead = messageObject.getBoolean("isRead");
                if (!isRead) {
                    return true; // Found an unread message
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
        return false; // No unread messages found
    }
}
