package com.buttonpilot.app.service.recording

import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import com.buttonpilot.app.core.common.Constants
import com.buttonpilot.app.core.logging.DiagnosticLogger
import com.buttonpilot.app.core.storage.MediaStoreHelper
import com.buttonpilot.app.data.local.AppDatabase
import com.buttonpilot.app.data.local.RecordingEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaStoreHelper: MediaStoreHelper,
    private val database: AppDatabase,
    private val logger: DiagnosticLogger
) {
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var startTime: Long = 0L
    private var elapsedJob: Job? = null
    private val stateMutex = Mutex()
    private var currentTrigger: String = "Manual"

    suspend fun startRecording(trigger: String = "Manual"): Boolean = stateMutex.withLock {
        val current = _recordingState.value
        if (current is RecordingState.Recording) {
            logger.w("RecordingManager", "Already recording")
            return false
        }
        if (current is RecordingState.Preparing || current is RecordingState.Stopping) {
            logger.w("RecordingManager", "Transition state, cannot start: $current")
            return false
        }

        try {
            _recordingState.value = RecordingState.Preparing
            currentTrigger = trigger
            logger.i("RecordingManager", "Starting recording, trigger: $trigger")

            val file = mediaStoreHelper.createRecordingFile()
            currentFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            startTime = System.currentTimeMillis()
            _recordingState.value = RecordingState.Recording(startTime, 0L)
            startElapsedTimer()

            // Start foreground service
            val serviceIntent = Intent(context, AudioRecordingService::class.java).apply {
                action = Constants.ACTION_START_RECORDING
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            logger.i("RecordingManager", "Recording started: ${file.absolutePath}")
            true
        } catch (e: Exception) {
            logger.e("RecordingManager", "Failed to start recording: ${e.message}", e)
            _recordingState.value = RecordingState.Error(e.message ?: "Failed to start")
            cleanupRecorder()
            false
        }
    }

    suspend fun stopRecording(): Boolean = stateMutex.withLock {
        val current = _recordingState.value
        if (current !is RecordingState.Recording && current !is RecordingState.Paused) {
            logger.w("RecordingManager", "Not recording, cannot stop: $current")
            return false
        }

        try {
            _recordingState.value = RecordingState.Stopping
            logger.i("RecordingManager", "Stopping recording")

            elapsedJob?.cancel()
            elapsedJob = null

            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: Exception) {
                    logger.e("RecordingManager", "Stop failed: ${e.message}")
                }
                try {
                    release()
                } catch (e: Exception) {
                    logger.e("RecordingManager", "Release failed: ${e.message}")
                }
            }
            mediaRecorder = null

            val file = currentFile
            val duration = System.currentTimeMillis() - startTime

            if (file != null && file.exists() && file.length() > 0) {
                // Save metadata
                val entity = RecordingEntity(
                    filename = file.name,
                    path = file.absolutePath,
                    uri = file.absolutePath,
                    createdAt = startTime,
                    duration = duration,
                    size = file.length(),
                    sourceShortcut = currentTrigger
                )
                CoroutineScope(Dispatchers.IO).launch {
                    database.recordingDao().insert(entity)
                }
                mediaStoreHelper.addToMediaStore(file)

                _recordingState.value = RecordingState.Saved(file.absolutePath, duration)
                logger.i("RecordingManager", "Recording saved: ${file.absolutePath}, duration: $duration")
            } else {
                _recordingState.value = RecordingState.Error("File empty or missing")
                logger.e("RecordingManager", "File empty or missing after stop")
            }

            currentFile = null
            _elapsedTime.value = 0L

            // Stop service
            val stopIntent = Intent(context, AudioRecordingService::class.java).apply {
                action = Constants.ACTION_STOP_RECORDING
            }
            context.startService(stopIntent)

            true
        } catch (e: Exception) {
            logger.e("RecordingManager", "Failed to stop recording: ${e.message}", e)
            _recordingState.value = RecordingState.Error(e.message ?: "Failed to stop")
            cleanupRecorder()
            false
        }
    }

    suspend fun pauseRecording(): Boolean = stateMutex.withLock {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false
        val current = _recordingState.value
        if (current !is RecordingState.Recording) return false

        try {
            mediaRecorder?.pause()
            _recordingState.value = RecordingState.Paused(System.currentTimeMillis() - startTime)
            elapsedJob?.cancel()
            true
        } catch (e: Exception) {
            logger.e("RecordingManager", "Pause failed: ${e.message}")
            false
        }
    }

    suspend fun resumeRecording(): Boolean = stateMutex.withLock {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false
        val current = _recordingState.value
        if (current !is RecordingState.Paused) return false

        try {
            mediaRecorder?.resume()
            _recordingState.value = RecordingState.Recording(startTime, current.elapsedMs)
            startElapsedTimer()
            true
        } catch (e: Exception) {
            logger.e("RecordingManager", "Resume failed: ${e.message}")
            false
        }
    }

    fun handleAudioInterruption() {
        CoroutineScope(Dispatchers.IO).launch {
            logger.w("RecordingManager", "Audio interruption - stopping safely")
            stopRecording()
        }
    }

    private fun startElapsedTimer() {
        elapsedJob?.cancel()
        elapsedJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(1000)
                val elapsed = System.currentTimeMillis() - startTime
                _elapsedTime.value = elapsed
                val current = _recordingState.value
                if (current is RecordingState.Recording) {
                    _recordingState.value = current.copy(elapsedMs = elapsed)
                }
            }
        }
    }

    private fun cleanupRecorder() {
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            // ignore
        }
        mediaRecorder = null
        elapsedJob?.cancel()
        currentFile?.delete()
        currentFile = null
    }

    fun getCurrentFile(): File? = currentFile
}
