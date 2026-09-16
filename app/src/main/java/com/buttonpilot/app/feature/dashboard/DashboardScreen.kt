package com.buttonpilot.app.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buttonpilot.app.service.recording.RecordingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToRecorder: () -> Unit,
    onNavigateToRecordings: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToPermissionCenter: () -> Unit,
    onNavigateToTestShortcut: () -> Unit,
    onNavigateToShortcutBuilder: () -> Unit,
    onNavigateToBackgroundReliability: () -> Unit,
    onNavigateToOnboarding: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ButtonPilot", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
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
            // Header status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (uiState.accessibilityEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ButtonPilot", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (uiState.accessibilityEnabled) "Hardware Automation: ACTIVE" else "Hardware Automation: INACTIVE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Triple Volume Down toggles voice recording. Works even when app is backgrounded (subject to Android restrictions).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Status cards grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusCard(
                    modifier = Modifier.weight(1f),
                    title = "Accessibility Service",
                    value = if (uiState.accessibilityEnabled) "Enabled" else "Disabled",
                    isActive = uiState.accessibilityEnabled,
                    icon = Icons.Default.Accessibility
                )
                StatusCard(
                    modifier = Modifier.weight(1f),
                    title = "Root Enhanced",
                    value = if (uiState.rootEnhancedEnabled) "Enabled" else if (uiState.rootAvailable) "Available" else "Unavailable",
                    isActive = uiState.rootEnhancedEnabled,
                    icon = Icons.Default.Security
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusCard(
                    modifier = Modifier.weight(1f),
                    title = "Recording Service",
                    value = when (uiState.recordingState) {
                        is RecordingState.Recording -> "Recording"
                        is RecordingState.Paused -> "Paused"
                        else -> "Idle"
                    },
                    isActive = uiState.isRecording,
                    icon = Icons.Default.Mic
                )
                StatusCard(
                    modifier = Modifier.weight(1f),
                    title = "Shortcuts Enabled",
                    value = if (uiState.shortcutsEnabled) "Yes" else "No",
                    isActive = uiState.shortcutsEnabled,
                    icon = Icons.Default.Bolt
                )
            }

            // Primary quick-action card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("TRIPLE VOLUME DOWN", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Voice Recorder Toggle", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                if (uiState.shortcutsEnabled) "ENABLED" else "DISABLED",
                                color = if (uiState.shortcutsEnabled) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onNavigateToRecorder, modifier = Modifier.weight(1f)) {
                            Text("Open Recorder")
                        }
                        OutlinedButton(onClick = onNavigateToTestShortcut, modifier = Modifier.weight(1f)) {
                            Text("Test Shortcut")
                        }
                    }
                }
            }

            // Action buttons
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ElevatedButton(onClick = onNavigateToPermissionCenter, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Permissions")
                }
                ElevatedButton(onClick = onNavigateToRecordings, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.LibraryMusic, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Recordings")
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ElevatedButton(onClick = onNavigateToShortcutBuilder, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Build, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Shortcut Builder")
                }
                ElevatedButton(onClick = onNavigateToDiagnostics, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.BugReport, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Diagnostics")
                }
            }
            OutlinedButton(onClick = onNavigateToBackgroundReliability, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.BatteryChargingFull, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Background Reliability")
            }

            // Privacy note
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Privacy Design", fontWeight = FontWeight.Bold)
                    Text(
                        "• No cloud upload by default\n• Recordings remain local unless you share\n• No hidden recordings - visible notification while recording\n• No remote activation\n• Device-owner utility only",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun StatusCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    isActive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, maxLines = 2)
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
