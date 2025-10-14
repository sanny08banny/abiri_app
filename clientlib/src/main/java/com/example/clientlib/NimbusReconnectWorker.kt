package com.example.clientlib


import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class NimbusReconnectWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        return try {
            val prefs = applicationContext.getSharedPreferences("nimbus_prefs", Context.MODE_PRIVATE)
            val deviceId = prefs.getString("nimbus_id", null)

            if (deviceId != null) {
                if (!NimbusWebSocket.isConnected()) {
                    Log.d("NimbusReconnectWorker", "Socket not connected → reconnecting...")
                    NimbusWebSocket.connect(deviceId, applicationContext)
                } else {
                    Log.d("NimbusReconnectWorker", "Already connected, skipping reconnect.")
                }
            } else {
                Log.w("NimbusReconnectWorker", "No Nimbus ID found; skipping reconnect.")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("NimbusReconnectWorker", "Error during reconnect", e)
            Result.retry() // Auto retry on next schedule
        }
    }
}
