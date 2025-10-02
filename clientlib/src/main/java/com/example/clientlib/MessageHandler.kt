package com.example.clientlib

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object MessageHandler {
    @SuppressLint("MissingPermission")
    fun onMessageReceived(context: Context, message: String) {
        Log.d("NimbusPush", "Handling message: $message")

        // Example: show a local notification
        val notification = NotificationCompat.Builder(context, NimbusPushService.NOTIF_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("New Message")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }
}
