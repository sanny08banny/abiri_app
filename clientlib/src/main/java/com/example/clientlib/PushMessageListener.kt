package com.example.clientlib

import android.content.Context

interface PushMessageListener {
    fun onMessageReceived(message: String)
}
