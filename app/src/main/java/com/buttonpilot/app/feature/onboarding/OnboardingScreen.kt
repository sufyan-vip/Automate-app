package com.buttonpilot.app.feature.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buttonpilot.app.core.permissions.PermissionManager
import com.buttonpilot.app.core.storage.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    val permissionManager: PermissionManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesManager.setOnboardingCompleted(true)
        }
    }
}

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onFinished: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    val context = LocalContext.current
    var micGranted by remember { mutableStateOf(viewModel.permissionManager.isMicrophoneGranted()) }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        micGranted = granted
        if (granted) step = 3
    }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        step = 5
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Text("Welcome to ButtonPilot", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Hardware Button Automation", style = MaterialTheme.typography.titleMedium)

        when (step) {
            1 -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("What the app does", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("• Triple-press Volume Down to toggle voice recording\n• Assign actions to hardware button patterns\n• Works even when app is backgrounded (subject to Android restrictions)\n• Privacy-first: local recordings, visible notification, no cloud upload")
                    }
                }
                Button(onClick = { step = 2 }, modifier = Modifier.fillMaxWidth()) { Text("Next") }
            }
            2 -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Step 2: Microphone Permission", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("Required for voice recorder. Recording uses Android's required microphone permission and foreground-service notification.")
                        if (micGranted) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text("Granted", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                if (!micGranted) {
                    Button(onClick = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Grant Microphone")
                    }
                } else {
                    Button(onClick = { step = 3 }, modifier = Modifier.fillMaxWidth()) { Text("Next") }
                }
            }
            3 -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Step 3: Enable Accessibility", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("ButtonPilot uses Accessibility to detect hardware button patterns like triple-press Volume Down. It does not read screen content or collect passwords. Enable to allow global hardware button detection.")
                        Text("Why hardware-key access is needed: Android only allows global volume key detection via Accessibility Service.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Button(onClick = {
                    context.startActivity(viewModel.permissionManager.getAccessibilitySettingsIntent())
                    step = 4
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Open Accessibility Settings")
                }
                OutlinedButton(onClick = { step = 4 }, modifier = Modifier.fillMaxWidth()) { Text("Skip / Next") }
            }
            4 -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Step 4: Notification Permission", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("Required for foreground-service notification while recording. Android requires visible indicator for microphone use.")
                    }
                }
                Button(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        step = 5
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) "Grant Notification" else "Next")
                }
            }
            5 -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Step 5: Test Triple Volume Down", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("Press Volume Down 3 times rapidly:\n\n• First triple press: start voice recording\n• Second triple press: stop and save\n\nIf Android blocks background mic access, you'll get a notification to start recording in foreground.")
                        Text("Default timing: max interval 450ms, sequence timeout 1100ms", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Button(onClick = { step = 6 }, modifier = Modifier.fillMaxWidth()) { Text("I Tested / Next") }
            }
            6 -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Step 6: Optional Root Enhanced Mode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("For device owner's own rooted device only. Improves hardware-key monitoring on devices where standard APIs are insufficient.\n\n• Normal features do NOT require root\n• Root is isolated behind dedicated interface\n• Never accepts arbitrary shell commands\n• No network shell\n• Shows exactly what needs root")
                        Text("Do NOT present root as mandatory. It's optional.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
                Button(onClick = {
                    viewModel.completeOnboarding()
                    onFinished()
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Finish Setup")
                }
            }
        }

        LinearProgressIndicator(progress = step / 6f, modifier = Modifier.fillMaxWidth())
        Text("Step $step of 6")
    }
}
