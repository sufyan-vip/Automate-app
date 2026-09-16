package com.buttonpilot.app.feature.diagnostics

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.buttonpilot.app.core.logging.DiagnosticLogger
import com.buttonpilot.app.core.permissions.PermissionManager
import com.buttonpilot.app.core.root.RootController
import com.buttonpilot.app.data.local.AppDatabase
import com.buttonpilot.app.feature.automation.HardwareShortcutDetector
import com.buttonpilot.app.service.accessibility.AccessibilityInputSource
import com.buttonpilot.app.service.recording.RecordingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    val permissionManager: PermissionManager,
    val rootController: RootController,
    val logger: DiagnosticLogger,
    val recordingManager: RecordingManager,
    val detector: HardwareShortcutDetector,
    val accessibilitySource: AccessibilityInputSource,
    val database: AppDatabase
) : ViewModel() {
    var lastEvents by mutableStateOf<List<String>>(emptyList())
    var detectorState by mutableStateOf(detector.getCurrentState())
    var recordingState by mutableStateOf("Unknown")

    init {
        viewModelScope.launch {
            recordingManager.recordingState.collectLatest { state ->
                recordingState = state::class.simpleName ?: "Unknown"
            }
        }
    }

    fun refreshDetector() {
        detectorState = detector.getCurrentState()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    viewModel: DiagnosticsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val logs by viewModel.logger.logs.collectAsState()
    var rootAvailable by remember { mutableStateOf(false) }
    var history by remember { mutableStateOf<List<com.buttonpilot.app.data.local.ActionHistoryEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        rootAvailable = viewModel.rootController.isRootAvailable()
        viewModel.database.actionHistoryDao().getRecent(20).collectLatest { history = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Diagnostics") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
            }, actions = {
                IconButton(onClick = {
                    val text = viewModel.logger.getExportText()
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    context.startActivity(Intent.createChooser(intent, "Export Diagnostic Log"))
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Export")
                }
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("System Info", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            InfoRow("Android Version", "${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            InfoRow("Manufacturer/Model", "${Build.MANUFACTURER} ${Build.MODEL}")
            InfoRow("Target SDK", "34")
            InfoRow("Accessibility Status", if (viewModel.permissionManager.isAccessibilityEnabled()) "Enabled" else "Disabled")
            InfoRow("Root Availability", if (rootAvailable) "Available" else "Unavailable")
            InfoRow("Notification Permission", if (viewModel.permissionManager.isNotificationGranted()) "Granted" else "Not Granted")
            InfoRow("Microphone Permission", if (viewModel.permissionManager.isMicrophoneGranted()) "Granted" else "Not Granted")
            InfoRow("Battery Optimization", if (viewModel.permissionManager.isBatteryOptimizationIgnored()) "Ignored (Good)" else "Not Ignored")
            InfoRow("Active Input Source", viewModel.accessibilitySource.getSourceName())
            InfoRow("Current Detector State", "downCount=${viewModel.detectorState.downPressCount}, upCount=${viewModel.detectorState.upPressCount}, cooldown=${viewModel.detectorState.isInCooldown}")
            InfoRow("Foreground Recorder State", viewModel.recordingState)

            Divider()
            Text("Feature Compatibility", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            InfoRow("Triple Volume Down", "Supported")
            InfoRow("Screen-Off Detection", if (Build.MANUFACTURER.lowercase().contains("xiaomi") || Build.MANUFACTURER.lowercase().contains("oppo")) "Limited (OEM)" else "Supported")
            InfoRow("Root Input Adapter", if (rootAvailable) "Available" else "Unavailable")
            InfoRow("Background Recording Start", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) "Platform Restricted - requires foreground" else "Supported with notification")

            Divider()
            Text("Last 20 Shortcut Events (Action History)", fontWeight = FontWeight.Bold)
            if (history.isEmpty()) {
                Text("No history yet", style = MaterialTheme.typography.bodySmall)
            } else {
                history.forEach { h ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("${java.text.SimpleDateFormat("MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date(h.timestamp))} - ${h.shortcutName}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text("${h.action} -> ${h.result}", style = MaterialTheme.typography.bodySmall)
                            if (h.errorCode != null) Text("Error: ${h.errorCode}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Divider()
            Text("Diagnostic Logs (last ${logs.size})", fontWeight = FontWeight.Bold)
            if (logs.isEmpty()) {
                Text("No logs", style = MaterialTheme.typography.bodySmall)
            } else {
                logs.take(20).forEach { log ->
                    Text(log.formatted(), style = MaterialTheme.typography.labelSmall)
                }
            }

            Button(onClick = { viewModel.refreshDetector() }, modifier = Modifier.fillMaxWidth()) {
                Text("Refresh Detector State")
            }
            OutlinedButton(onClick = { viewModel.logger.clear() }, modifier = Modifier.fillMaxWidth()) {
                Text("Clear Logs")
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
    }
}
