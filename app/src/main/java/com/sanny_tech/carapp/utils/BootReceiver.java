package com.sanny_tech.carapp.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.clientlib.NimbusPushService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NimbusPushService.Companion.start(context);
//              NimbusWebSocket.INSTANCE.registerListener(new MyPushHandler());
            } else {
                NimbusPushService.Companion.start(context);
//              NimbusWebSocket.INSTANCE.registerListener(new MyPushHandler());
            }
        }
    }
}

