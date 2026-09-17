package com.buttonpilot.app.service

import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RecordingManager {
    @Volatile private var recording = false
    @Volatile private var recorder: MediaRecorder? = null
    @Volatile private var startTime: Long = 0L

    fun isRecording(): Boolean = recording

    fun start(context: Context): Boolean {
        if (recording) return false
        return try {
            val dir = File(context.getExternalFilesDir(null), "recordings").apply { mkdirs() }
            val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
            val file = File(dir, "BP_REC_${sdf.format(Date())}.m4a")

            val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mr.setAudioSource(MediaRecorder.AudioSource.MIC)
            mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mr.setAudioEncodingBitRate(128000)
            mr.setAudioSamplingRate(44100)
            mr.setOutputFile(file.absolutePath)
            mr.prepare()
            mr.start()
            recorder = mr
            startTime = System.currentTimeMillis()
            recording = true

            val svc = Intent(context, AudioRecordingService::class.java).apply {
                action = AudioRecordingService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(svc)
            else context.startService(svc)
            true
        } catch (e: Exception) {
            cleanup(); false
        }
    }

    fun stop(context: Context): Boolean {
        if (!recording) return false
        return try {
            recorder?.apply { stop(); release() }
            recording = false
            cleanup()
            context.startService(Intent(context, AudioRecordingService::class.java).apply {
                action = AudioRecordingService.ACTION_STOP
            })
            true
        } catch (e: Exception) {
            cleanup(); false
        }
    }

    fun elapsedMs(): Long = if (recording) System.currentTimeMillis() - startTime else 0L

    private fun cleanup() {
        try { recorder?.release() } catch (_: Exception) {}
        recorder = null
    }
}
