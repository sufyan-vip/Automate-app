package com.buttonpilot.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioRecordingService : Service() {

    companion object {
        const val ACTION_TOGGLE = "com.buttonpilot.app.action.TOGGLE"
        private const val ACTION_STOP_SELF = "com.buttonpilot.app.action.STOP_SELF"
    }

    private var recorder: MediaRecorder? = null
    private var recording = false
    private var startTime: Long = 0L
    private var currentFile: File? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var tickJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SELF -> {
                stopRecording()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE -> {
                if (recording) {
                    stopRecording()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    startRecording()
                    startFg()
                }
            }
        }
        return START_STICKY
    }

    private fun startRecording() {
        try {
            val dir = File(getExternalFilesDir(null), "recordings").apply { mkdirs() }
            val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
            val file = File(dir, "BP_REC_${sdf.format(Date())}.m4a")
            val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(this) else @Suppress("DEPRECATION") MediaRecorder()
            mr.setAudioSource(MediaRecorder.AudioSource.MIC)
            mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mr.setAudioEncodingBitRate(128000)
            mr.setAudioSamplingRate(44100)
            mr.setOutputFile(file.absolutePath)
            mr.prepare()
            mr.start()
            recorder = mr
            currentFile = file
            startTime = System.currentTimeMillis()
            recording = true
            getSharedPreferences("state", Context.MODE_PRIVATE).edit().putBoolean("recording", true).apply()
        } catch (e: Exception) {
            e.printStackTrace()
            cleanup()
        }
    }

    private fun stopRecording() {
        try { recorder?.apply { stop(); release() } } catch (_: Exception) {}
        recording = false
        getSharedPreferences("state", Context.MODE_PRIVATE).edit().putBoolean("recording", false).apply()
        cleanup()
    }

    private fun cleanup() {
        try { recorder?.release() } catch (_: Exception) {}
        recorder = null
    }

    private fun startFg() {
        val notif = buildNotif("00:00")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(ButtonPilotApp.NOTIF_ID_RECORDING, notif, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(ButtonPilotApp.NOTIF_ID_RECORDING, notif)
        }
        tickJob?.cancel()
        tickJob = scope.launch {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            while (recording) {
                delay(1000)
                val ms = System.currentTimeMillis() - startTime
                val text = String.format(Locale.US, "%02d:%02d", (ms / 1000 / 60) % 60, ms / 1000 % 60)
                nm.notify(ButtonPilotApp.NOTIF_ID_RECORDING, buildNotif(text))
            }
        }
    }

    private fun buildNotif(elapsed: String): Notification {
        val openPi = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stopPi = PendingIntent.getService(this, 1,
            Intent(this, AudioRecordingService::class.java).apply { action = ACTION_STOP_SELF },
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
        stopRecording()
        super.onDestroy()
    }
}
