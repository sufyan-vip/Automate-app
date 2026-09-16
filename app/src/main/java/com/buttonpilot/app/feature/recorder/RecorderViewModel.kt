package com.buttonpilot.app.feature.recorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buttonpilot.app.core.permissions.PermissionManager
import com.buttonpilot.app.service.recording.RecordingManager
import com.buttonpilot.app.service.recording.RecordingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecorderViewModel @Inject constructor(
    private val recordingManager: RecordingManager,
    val permissionManager: PermissionManager
) : ViewModel() {

    val recordingState: StateFlow<RecordingState> = recordingManager.recordingState
    val elapsedTime: StateFlow<Long> = recordingManager.elapsedTime

    fun startRecording() {
        viewModelScope.launch {
            recordingManager.startRecording("UI Button")
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            recordingManager.stopRecording()
        }
    }

    fun pauseRecording() {
        viewModelScope.launch {
            recordingManager.pauseRecording()
        }
    }

    fun resumeRecording() {
        viewModelScope.launch {
            recordingManager.resumeRecording()
        }
    }
}
