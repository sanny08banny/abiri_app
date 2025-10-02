package com.sanny_tech.carapp.communication_utils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class SmsSender {

//    private static String ACCOUNT_SID;
//    private static String AUTH_TOKEN;
//    private static String TWILIO_PHONE_NUMBER;
//
//    public static void initializeTwilio() {
//        DatabaseReference configRef = FirebaseDatabase.getInstance().getReference("configurations");
//
//        configRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                ACCOUNT_SID = dataSnapshot.child("twilioAccountSid").getValue(String.class);
//                AUTH_TOKEN = dataSnapshot.child("twilioAuthToken").getValue(String.class);
//                TWILIO_PHONE_NUMBER = "+" + dataSnapshot.child("twilioPhoneNumber").getValue(String.class);
//
//                Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                // Handle error
//            }
//        });
//    }
//
//    public static void sendSms(String to, String body) {
//        if (ACCOUNT_SID == null || AUTH_TOKEN == null || TWILIO_PHONE_NUMBER == null) {
//            throw new IllegalStateException("Twilio is not initialized. Call initializeTwilio() first.");
//        }
//
//        Message message = Message.creator(new PhoneNumber(to), new PhoneNumber(TWILIO_PHONE_NUMBER), body).create();
//    }
}

