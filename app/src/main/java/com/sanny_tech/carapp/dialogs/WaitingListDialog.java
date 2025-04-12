package com.sanny_tech.carapp.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class WaitingListDialog {

    public static void show(Context context, final String serviceName, String userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Create a SpannableStringBuilder to customize the title text
        SpannableStringBuilder titleBuilder = new SpannableStringBuilder();
        int serviceNameStart = titleBuilder.length(); // Get the starting position of the service name
        titleBuilder.append("Join");
        titleBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), serviceNameStart, titleBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        titleBuilder.append(" the waiting list for ");
        int joinStart = titleBuilder.length(); // Get the starting position of the "Join" word
        titleBuilder.append(serviceName);
        titleBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), joinStart, titleBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        titleBuilder.append(" !");
        builder.setMessage(titleBuilder)
                .setPositiveButton("Join", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        addUserToWaitingList(serviceName,userId, context);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private static void addUserToWaitingList(String serviceName, String userId, Context context) {
        DatabaseReference waitingListRef = FirebaseDatabase.getInstance().
                getReference("waiting_list");
        waitingListRef.child(serviceName).child(userId).setValue(true);
        Toast.makeText(context, "Joined successfully", Toast.LENGTH_SHORT).show();
    }

}

