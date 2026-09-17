package com.buttonpilot.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class ButtonPilotApp : Application() {
    companion object {
        const val CHANNEL_RECORDING = "recording_channel"
        const val CHANNEL_GENERAL = "general_channel"
        const val NOTIF_ID_RECORDING = 1001
        const val ACTION_STOP = "com.buttonpilot.app.action.STOP"
        const val ACTION_START = "com.buttonpilot.app.action.START"
        const val ACTION_TOGGLE = "com.buttonpilot.app.action.TOGGLE"
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val rec = NotificationChannel(CHANNEL_RECORDING, "Voice Recording", NotificationManager.IMPORTANCE_LOW)
                .apply { description = "Active voice recording indicator" }
            val gen = NotificationChannel(CHANNEL_GENERAL, "General", NotificationManager.IMPORTANCE_HIGH)
            getSystemService(NotificationManager::class.java).apply {
                createNotificationChannel(rec)
                createNotificationChannel(gen)
            }
        }
    }
}
