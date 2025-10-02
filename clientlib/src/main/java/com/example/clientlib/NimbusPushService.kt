package com.example.clientlib

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.util.UUID

class NimbusPushService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIF_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val deviceId = intent?.getStringExtra(EXTRA_DEVICE_ID)
        if (deviceId != null) {
            NimbusWebSocket.connect(deviceId,applicationContext)
        } else {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        NimbusWebSocket.disconnect()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setContentTitle("Nimbus Push")
            .setContentText("Connected to message dispatcher")
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setSound(null)
            .setVibrate(null)
            .build()
    }

    companion object {
        const val NOTIF_ID = 101
        const val NOTIF_CHANNEL_ID = "nimbus_push_channel"
        const val EXTRA_DEVICE_ID = "device_id"

        fun start(context: Context) {
            createNotificationChannel(context)

            val id = getNimbusId(context)
            val intent = Intent(context, NimbusPushService::class.java).apply {
                putExtra(EXTRA_DEVICE_ID, id)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    NOTIF_CHANNEL_ID,
                    "Nimbus Push Channel",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    setSound(null, null)
                    enableVibration(false)
                    setShowBadge(false)
                }
                val manager = context.getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(channel)
            }
        }
        fun getNimbusId(context: Context): String {
            val prefs = context.getSharedPreferences("nimbus_prefs", Context.MODE_PRIVATE)
            val existingId = prefs.getString("nimbus_id", null)

            return if (existingId != null) {
                existingId
            } else {
                val newId = UUID.randomUUID().toString()
                prefs.edit().putString("nimbus_id", newId).apply()
                newId
            }
        }

    }
}


