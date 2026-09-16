package com.buttonpilot.app.feature.automation

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.provider.MediaStore
import com.buttonpilot.app.core.haptics.HapticFeedbackManager
import com.buttonpilot.app.core.logging.DiagnosticLogger
import com.buttonpilot.app.core.permissions.PermissionManager
import com.buttonpilot.app.core.storage.PreferencesManager
import com.buttonpilot.app.data.local.ActionHistoryEntity
import com.buttonpilot.app.data.local.AppDatabase
import com.buttonpilot.app.service.recording.RecordingManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShortcutActionDispatcher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recordingManager: RecordingManager,
    private val permissionManager: PermissionManager,
    private val preferencesManager: PreferencesManager,
    private val haptics: HapticFeedbackManager,
    private val logger: DiagnosticLogger,
    private val database: AppDatabase
) {

    sealed class DispatchResult {
        data object Success : DispatchResult()
        data class Failure(val message: String) : DispatchResult()
        data object PermissionRequired : DispatchResult()
    }

    suspend fun dispatch(event: ShortcutEvent, rule: ShortcutRule? = null): DispatchResult {
        val action = rule?.action ?: mapEventToDefaultAction(event) ?: return DispatchResult.Failure("No action mapped")

        // Validate permission and state
        val shortcutsEnabled = try { preferencesManager.shortcutsEnabled.first() } catch (e: Exception) { true }
        if (!shortcutsEnabled) {
            return DispatchResult.Failure("Shortcuts disabled")
        }

        haptics.vibrateShortcutRecognized()
        logger.i("Dispatcher", "Dispatching $event -> $action")

        val result = when (action) {
            is ShortcutAction.ToggleRecording -> handleToggleRecording(event)
            is ShortcutAction.ToggleFlashlight -> handleFlashlight()
            is ShortcutAction.MediaPlayPause -> handleMediaPlayPause()
            is ShortcutAction.MediaNext -> handleMediaNext()
            is ShortcutAction.MediaPrevious -> handleMediaPrevious()
            is ShortcutAction.OpenCamera -> handleOpenCamera()
            is ShortcutAction.OpenRecorderLibrary -> DispatchResult.Success // Handled by UI
            is ShortcutAction.LaunchApp -> handleLaunchApp(action.packageName)
            else -> handleGenericAction(action)
        }

        // Write optional history
        try {
            val historyEnabled = preferencesManager.historyEnabled.first()
            if (historyEnabled) {
                val entity = ActionHistoryEntity(
                    timestamp = System.currentTimeMillis(),
                    shortcutName = event::class.simpleName ?: "Unknown",
                    action = action.displayName(),
                    result = result::class.simpleName ?: "Unknown",
                    errorCode = if (result is DispatchResult.Failure) result.message else null
                )
                CoroutineScope(Dispatchers.IO).launch {
                    database.actionHistoryDao().insert(entity)
                }
            }
        } catch (e: Exception) {
            logger.e("Dispatcher", "Failed to write history: ${e.message}")
        }

        return result
    }

    private fun mapEventToDefaultAction(event: ShortcutEvent): ShortcutAction? {
        return when (event) {
            is ShortcutEvent.TripleVolumeDown -> ShortcutAction.ToggleRecording
            is ShortcutEvent.DoubleVolumeUp -> ShortcutAction.ToggleFlashlight
            is ShortcutEvent.TripleVolumeUp -> ShortcutAction.OpenCamera
            is ShortcutEvent.DoubleVolumeDown -> ShortcutAction.MediaPlayPause
            else -> null
        }
    }

    private suspend fun handleToggleRecording(trigger: ShortcutEvent): DispatchResult {
        if (!permissionManager.isMicrophoneGranted()) {
            return DispatchResult.PermissionRequired
        }

        return try {
            val state = recordingManager.recordingState.first()
            when (state) {
                is com.buttonpilot.app.service.recording.RecordingState.Idle,
                is com.buttonpilot.app.service.recording.RecordingState.Saved,
                is com.buttonpilot.app.service.recording.RecordingState.Error -> {
                    val started = recordingManager.startRecording(trigger.toString())
                    if (started) {
                        haptics.vibrateRecordingStart()
                        DispatchResult.Success
                    } else {
                        DispatchResult.Failure("Could not start recording - Android may block background mic access")
                    }
                }
                is com.buttonpilot.app.service.recording.RecordingState.Recording,
                is com.buttonpilot.app.service.recording.RecordingState.Paused -> {
                    recordingManager.stopRecording()
                    haptics.vibrateRecordingStop()
                    DispatchResult.Success
                }
                else -> DispatchResult.Failure("Recording in transition")
            }
        } catch (e: Exception) {
            logger.e("Dispatcher", "Recording toggle failed: ${e.message}", e)
            DispatchResult.Failure(e.message ?: "Recording error")
        }
    }

    private fun handleFlashlight(): DispatchResult {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return DispatchResult.Failure("No camera")
            // Toggle logic simplified - actual implementation would track state
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // For demo, just turn on; real app would track toggle state
                cameraManager.setTorchMode(cameraId, true)
            }
            DispatchResult.Success
        } catch (e: Exception) {
            DispatchResult.Failure("Flashlight error: ${e.message}")
        }
    }

    private fun handleMediaPlayPause(): DispatchResult {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val keyEventDown = android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            val keyEventUp = android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            audioManager.dispatchMediaKeyEvent(keyEventDown)
            audioManager.dispatchMediaKeyEvent(keyEventUp)
            DispatchResult.Success
        } catch (e: Exception) {
            DispatchResult.Failure(e.message ?: "Media error")
        }
    }

    private fun handleMediaNext(): DispatchResult {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val down = android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_MEDIA_NEXT)
            val up = android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_MEDIA_NEXT)
            audioManager.dispatchMediaKeyEvent(down)
            audioManager.dispatchMediaKeyEvent(up)
            DispatchResult.Success
        } catch (e: Exception) {
            DispatchResult.Failure(e.message ?: "Media next error")
        }
    }

    private fun handleMediaPrevious(): DispatchResult {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val down = android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            val up = android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            audioManager.dispatchMediaKeyEvent(down)
            audioManager.dispatchMediaKeyEvent(up)
            DispatchResult.Success
        } catch (e: Exception) {
            DispatchResult.Failure(e.message ?: "Media previous error")
        }
    }

    private fun handleOpenCamera(): DispatchResult {
        return try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            DispatchResult.Success
        } catch (e: Exception) {
            DispatchResult.Failure(e.message ?: "Camera open failed")
        }
    }

    private fun handleLaunchApp(packageName: String): DispatchResult {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            } ?: return DispatchResult.Failure("App not found")
            context.startActivity(intent)
            DispatchResult.Success
        } catch (e: Exception) {
            DispatchResult.Failure(e.message ?: "Launch failed")
        }
    }

    private fun handleGenericAction(action: ShortcutAction): DispatchResult {
        logger.i("Dispatcher", "Generic action: $action - not fully implemented, returning success for UI handling")
        return DispatchResult.Success
    }
}
