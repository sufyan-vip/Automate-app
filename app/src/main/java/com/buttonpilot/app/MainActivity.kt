package com.buttonpilot.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.buttonpilot.app.service.AudioRecordingService
import com.buttonpilot.app.service.RecordingManager
import com.buttonpilot.app.ui.theme.ButtonPilotTheme

class MainActivity : ComponentActivity() {

    private val micLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { refresh() }
    private val notifLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { refresh() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ButtonPilotTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Dashboard(
                        onRequestMic = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        onRequestNotif = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        onOpenAccessibility = {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        },
                        onToggle = {
                            val i = Intent(this, AudioRecordingService::class.java).apply { action = AudioRecordingService.ACTION_TOGGLE }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i) else startService(i)
                        }
                    )
                }
            }
        }
    }

    private fun refresh() { /* recomposition happens on resume */ }

    override fun onResume() {
        super.onResume()
        window.decorView.postDelayed({ recreate() }, 400)
    }
}

@Composable
fun Dashboard(
    onRequestMic: () -> Unit,
    onRequestNotif: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onToggle: () -> Unit
) {
    val ctx = LocalContext.current
    val micGranted = remember { ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED }
    val notifGranted = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        else true
    }
    val a11yEnabled = remember {
        (Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: "").contains(ctx.packageName)
    }
    val recording = RecordingManager.isRecording()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("ButtonPilot", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Triple-press Volume Down → Toggle voice recording", style = MaterialTheme.typography.bodyLarge)

        Card(colors = CardDefaults.cardColors(
            containerColor = if (a11yEnabled) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.errorContainer)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Hardware Automation", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(if (a11yEnabled) "ACTIVE" else "INACTIVE — enable Accessibility service", fontWeight = FontWeight.Bold)
            }
        }
        StatusRow("Microphone", micGranted)
        StatusRow("Notifications", notifGranted)
        StatusRow("Accessibility", a11yEnabled)
        StatusRow("Recording", recording)

        Spacer(Modifier.height(4.dp))
        Button(onClick = onRequestMic, enabled = !micGranted, modifier = Modifier.fillMaxWidth()) {
            Text(if (micGranted) "Microphone: Granted" else "Grant Microphone")
        }
        Button(onClick = onRequestNotif, enabled = !notifGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU, modifier = Modifier.fillMaxWidth()) {
            Text(if (notifGranted) "Notifications: Granted" else "Grant Notifications")
        }
        OutlinedButton(onClick = onOpenAccessibility, modifier = Modifier.fillMaxWidth()) { Text("Open Accessibility Settings") }
        Spacer(Modifier.height(4.dp))
        Button(onClick = onToggle, modifier = Modifier.fillMaxWidth()) {
            Text(if (recording) "STOP RECORDING" else "START RECORDING (test)")
        }

        Spacer(Modifier.height(8.dp))
        Card { Column(modifier = Modifier.padding(12.dp)) {
            Text("How to use", fontWeight = FontWeight.Bold)
            Text("1. Grant Microphone permission.\n2. Enable ButtonPilot Accessibility.\n3. Press Volume Down 3 times quickly to start/stop recording.\n4. Persistent notification shows while recording.\n5. Files are saved to app's external recordings folder.", style = MaterialTheme.typography.bodySmall)
        }}
        Card { Column(modifier = Modifier.padding(12.dp)) {
            Text("Privacy", fontWeight = FontWeight.Bold)
            Text("No hidden recordings. Visible mic-indicator notification always shown. Data stays on device.", style = MaterialTheme.typography.bodySmall)
        }}
    }
}

@Composable
fun StatusRow(name: String, ok: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(name)
        Text(if (ok) "✓ OK" else "✗ Missing",
            color = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold)
    }
}
