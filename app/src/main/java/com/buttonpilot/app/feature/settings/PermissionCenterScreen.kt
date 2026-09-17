package com.buttonpilot.app.feature.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.buttonpilot.app.core.permissions.PermissionManager
import com.buttonpilot.app.core.permissions.PermissionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PermissionCenterViewModel @Inject constructor(
    val permissionManager: PermissionManager
) : ViewModel() {
    fun refresh() {}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionCenterScreen(
    viewModel: PermissionCenterViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var micGranted by remember { mutableStateOf(viewModel.permissionManager.isMicrophoneGranted()) }
    var notificationGranted by remember { mutableStateOf(viewModel.permissionManager.isNotificationGranted()) }
    var accessibilityEnabled by remember { mutableStateOf(viewModel.permissionManager.isAccessibilityEnabled()) }
    var batteryIgnored by remember { mutableStateOf(viewModel.permissionManager.isBatteryOptimizationIgnored()) }
    var dndGranted by remember { mutableStateOf(viewModel.permissionManager.hasDndAccess()) }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        micGranted = granted
    }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationGranted = granted
    }

    fun refreshAll() {
        micGranted = viewModel.permissionManager.isMicrophoneGranted()
        notificationGranted = viewModel.permissionManager.isNotificationGranted()
        accessibilityEnabled = viewModel.permissionManager.isAccessibilityEnabled()
        batteryIgnored = viewModel.permissionManager.isBatteryOptimizationIgnored()
        dndGranted = viewModel.permissionManager.hasDndAccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Permission Center") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
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
            PermissionCard(
                name = "Microphone",
                description = "Required for voice recorder. Toggle recording via triple Volume Down.",
                status = if (micGranted) PermissionStatus.Granted else PermissionStatus.NotGranted,
                isRequired = true,
                actionText = if (micGranted) "Granted" else "Grant",
                onAction = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }
            )
            PermissionCard(
                name = "Notifications",
                description = "Required/recommended for foreground-service notifications on supported Android versions.",
                status = if (notificationGranted) PermissionStatus.Granted else PermissionStatus.NotGranted,
                isRequired = true,
                actionText = if (notificationGranted) "Granted" else "Grant",
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } else {
                        context.startActivity(viewModel.permissionManager.getNotificationSettingsIntent())
                    }
                    refreshAll()
                }
            )
            PermissionCard(
                name = "Accessibility",
                description = "Required for supported global hardware-button detection. Does not read screen content.",
                status = if (accessibilityEnabled) PermissionStatus.Granted else PermissionStatus.NotGranted,
                isRequired = true,
                actionText = if (accessibilityEnabled) "Enabled" else "Enable",
                onAction = {
                    context.startActivity(viewModel.permissionManager.getAccessibilitySettingsIntent())
                }
            )
            PermissionCard(
                name = "Battery Optimization",
                description = "Optional explanation/settings shortcut. Disable to prevent OEM killing background detection.",
                status = if (batteryIgnored) PermissionStatus.Granted else PermissionStatus.NotGranted,
                isRequired = false,
                isOptional = true,
                actionText = if (batteryIgnored) "Ignored" else "Disable Optimization",
                onAction = {
                    try {
                        context.startActivity(viewModel.permissionManager.getBatteryOptimizationIntent())
                    } catch (e: Exception) {
                        context.startActivity(viewModel.permissionManager.getAppSettingsIntent())
                    }
                }
            )
            PermissionCard(
                name = "Do Not Disturb Access",
                description = "Only for DND actions. Optional.",
                status = if (dndGranted) PermissionStatus.Granted else PermissionStatus.NotGranted,
                isRequired = false,
                isOptional = true,
                actionText = if (dndGranted) "Granted" else "Grant",
                onAction = {
                    context.startActivity(viewModel.permissionManager.getDndAccessIntent())
                }
            )
            PermissionCard(
                name = "Root",
                description = "Optional. For device owner rooted device only. Improves hardware-key monitoring where standard APIs insufficient.",
                status = PermissionStatus.NotApplicable,
                isRequired = false,
                isOptional = true,
                actionText = "Check in Diagnostics",
                onAction = {}
            )

            Spacer(Modifier.height(16.dp))
            Button(onClick = { refreshAll() }, modifier = Modifier.fillMaxWidth()) {
                Text("Refresh Status")
            }
        }
    }
}

@Composable
fun PermissionCard(
    name: String,
    description: String,
    status: com.buttonpilot.app.core.permissions.PermissionStatus,
    isRequired: Boolean,
    isOptional: Boolean = false,
    actionText: String,
    onAction: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (status) {
                    is PermissionStatus.Granted -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    is PermissionStatus.NotGranted -> Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    else -> Icon(Icons.Default.Info, contentDescription = null)
                }
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Badge {
                    Text(
                        when (status) {
                            is PermissionStatus.Granted -> "Granted"
                            is PermissionStatus.NotGranted -> "Not Granted"
                            else -> "Optional"
                        }
                    )
                }
            }
            Text(description, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(if (isRequired) "Required" else if (isOptional) "Optional" else "Recommended") })
                when (status) {
                    is PermissionStatus.Granted -> AssistChip(onClick = {}, label = { Text("✓") })
                    else -> {}
                }
            }
            Button(onClick = onAction, enabled = status !is PermissionStatus.Granted || name == "Battery Optimization") {
                Text(actionText)
            }
        }
    }
}
