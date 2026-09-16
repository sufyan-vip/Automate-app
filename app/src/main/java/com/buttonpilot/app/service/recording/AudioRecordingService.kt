package com.buttonpilot.app.service.recording

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.buttonpilot.app.MainActivity
import com.buttonpilot.app.R
import com.buttonpilot.app.core.common.Constants
import com.buttonpilot.app.core.logging.DiagnosticLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AudioRecordingService : Service() {

    @Inject
    lateinit var recordingManager: RecordingManager

    @Inject
    lateinit var logger: DiagnosticLogger

    private var elapsedJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        logger.i("AudioRecordingService", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_START_RECORDING -> {
                startForegroundServiceWithNotification()
            }
            Constants.ACTION_STOP_RECORDING -> {
                stopForegroundService()
            }
            Constants.ACTION_PAUSE_RECORDING -> {
                scope.launch {
                    recordingManager.pauseRecording()
                }
            }
            Constants.ACTION_RESUME_RECORDING -> {
                scope.launch {
                    recordingManager.resumeRecording()
                }
            }
            else -> {
                startForegroundServiceWithNotification()
            }
        }
        return START_STICKY
    }

    private fun startForegroundServiceWithNotification() {
        val notification = buildNotification("00:00")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(Constants.NOTIFICATION_ID_RECORDING, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(Constants.NOTIFICATION_ID_RECORDING, notification)
        }

        elapsedJob?.cancel()
        elapsedJob = scope.launch {
            recordingManager.elapsedTime.collectLatest { elapsed ->
                val formatted = formatElapsed(elapsed)
                val updated = buildNotification(formatted)
                val nm = getSystemService(NotificationManager::class.java)
                nm.notify(Constants.NOTIFICATION_ID_RECORDING, updated)
            }
        }
        logger.i("AudioRecordingService", "Foreground started with microphone type")
    }

    private fun stopForegroundService() {
        elapsedJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        logger.i("AudioRecordingService", "Foreground stopped")
    }

    private fun buildNotification(elapsed: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPending = PendingIntent.getActivity(this, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, AudioRecordingService::class.java).apply {
            action = Constants.ACTION_STOP_RECORDING
        }
        val stopPending = PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val pauseIntent = Intent(this, AudioRecordingService::class.java).apply {
            action = Constants.ACTION_PAUSE_RECORDING
        }
        val pausePending = PendingIntent.getService(this, 2, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_RECORDING_ID)
            .setContentTitle(getString(com.buttonpilot.app.R.string.recording_in_progress))
            .setContentText("● $elapsed - Triggered by Volume Down ×3")
            .setSmallIcon(android.R.drawable.presence_audio_online)
            .setOngoing(true)
            .setContentIntent(openPending)
            .addAction(android.R.drawable.ic_media_pause, getString(com.buttonpilot.app.R.string.recording_pause), pausePending)
            .addAction(android.R.drawable.ic_media_pause, getString(com.buttonpilot.app.R.string.recording_stop), stopPending)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_RECORDING_ID,
                getString(com.buttonpilot.app.R.string.notification_channel_recording),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(com.buttonpilot.app.R.string.notification_channel_recording_desc)
                setShowBadge(false)
            }
            val generalChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_GENERAL_ID,
                getString(com.buttonpilot.app.R.string.notification_channel_general),
                NotificationManager.IMPORTANCE_HIGH
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
            nm.createNotificationChannel(generalChannel)
        }
    }

    private fun formatElapsed(ms: Long): String {
        val totalSec = ms / 1000
        val hours = totalSec / 3600
        val minutes = (totalSec % 3600) / 60
        val seconds = totalSec % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        elapsedJob?.cancel()
        super.onDestroy()
        logger.i("AudioRecordingService", "Service destroyed")
    }
}
