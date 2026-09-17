package com.buttonpilot.app.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.buttonpilot.app.ButtonPilotApp
import com.buttonpilot.app.MainActivity
import com.buttonpilot.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class AudioRecordingService : Service() {

    companion object {
        const val ACTION_START = "com.buttonpilot.app.action.START"
        const val ACTION_STOP = "com.buttonpilot.app.action.STOP"
        const val ACTION_TOGGLE = "com.buttonpilot.app.action.TOGGLE"
    }

    private val scope = CoroutineScope(Dispatchers.Main)
    private var tickJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TOGGLE -> {
                if (RecordingManager.isRecording()) {
                    RecordingManager.stop(this)
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    if (RecordingManager.start(this)) startFg() else stopSelf()
                }
            }
            ACTION_START -> if (RecordingManager.isRecording()) startFg()
            ACTION_STOP -> { stopForeground(STOP_FOREGROUND_REMOVE); stopSelf() }
        }
        return START_STICKY
    }

    private fun startFg() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(ButtonPilotApp.NOTIF_ID_RECORDING, buildNotif("00:00"),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(ButtonPilotApp.NOTIF_ID_RECORDING, buildNotif("00:00"))
        }
        tickJob?.cancel()
        tickJob = scope.launch {
            while (true) {
                delay(1000)
                val text = String.format(Locale.US, "%02d:%02d",
                    (RecordingManager.elapsedMs() / 1000 / 60) % 60,
                    RecordingManager.elapsedMs() / 1000 % 60)
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .notify(ButtonPilotApp.NOTIF_ID_RECORDING, buildNotif(text))
            }
        }
    }

    private fun buildNotif(elapsed: String): Notification {
        val openPi = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stopPi = PendingIntent.getService(this, 1,
            Intent(this, AudioRecordingService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, ButtonPilotApp.CHANNEL_RECORDING)
            .setContentTitle(getString(R.string.recording_in_progress))
            .setContentText("● $elapsed")
            .setSmallIcon(android.R.drawable.presence_audio_online)
            .setOngoing(true)
            .setContentIntent(openPi)
            .addAction(android.R.drawable.ic_media_pause, getString(R.string.recording_stop), stopPi)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    override fun onDestroy() {
        tickJob?.cancel()
        super.onDestroy()
    }
}
