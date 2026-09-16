package com.buttonpilot.app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buttonpilot.app.core.permissions.PermissionManager
import com.buttonpilot.app.core.root.RootController
import com.buttonpilot.app.core.storage.PreferencesManager
import com.buttonpilot.app.feature.automation.HardwareShortcutDetector
import com.buttonpilot.app.service.recording.RecordingManager
import com.buttonpilot.app.service.recording.RecordingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val accessibilityEnabled: Boolean = false,
    val rootAvailable: Boolean = false,
    val rootEnhancedEnabled: Boolean = false,
    val isRecording: Boolean = false,
    val shortcutsEnabled: Boolean = true,
    val detectorState: com.buttonpilot.app.feature.automation.DetectorState? = null,
    val recordingState: RecordingState = RecordingState.Idle
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    val permissionManager: PermissionManager,
    val preferencesManager: PreferencesManager,
    private val recordingManager: RecordingManager,
    private val rootController: RootController,
    private val detector: HardwareShortcutDetector
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.shortcutsEnabled.collectLatest { enabled ->
                _uiState.value = _uiState.value.copy(shortcutsEnabled = enabled)
            }
        }
        viewModelScope.launch {
            preferencesManager.rootEnhancedEnabled.collectLatest { enabled ->
                _uiState.value = _uiState.value.copy(rootEnhancedEnabled = enabled)
            }
        }
        viewModelScope.launch {
            recordingManager.recordingState.collectLatest { state ->
                _uiState.value = _uiState.value.copy(
                    recordingState = state,
                    isRecording = state is RecordingState.Recording
                )
            }
        }
        checkPermissions()
        viewModelScope.launch {
            val available = rootController.isRootAvailable()
            _uiState.value = _uiState.value.copy(rootAvailable = available)
        }
    }

    fun checkPermissions() {
        _uiState.value = _uiState.value.copy(
            accessibilityEnabled = permissionManager.isAccessibilityEnabled()
        )
    }

    fun refreshDetectorState() {
        _uiState.value = _uiState.value.copy(detectorState = detector.getCurrentState())
    }
}
