package com.buttonpilot.app.feature.recorder

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
fun RecorderScreen(
    viewModel: RecorderViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.recordingState.collectAsState()
    val elapsed by viewModel.elapsedTime.collectAsState()

    fun format(ms: Long): String {
        val total = ms / 1000
        val h = total / 3600
        val m = (total % 3600) / 60
        val s = total % 60
        return if (h > 0) String.format("%02d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Recorder") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (state) {
                is RecordingState.Idle, is RecordingState.Saved, is RecordingState.Error -> {
                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text("Ready", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Triple Volume Down to start", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Triggered by: Volume Down ×3", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { viewModel.startRecording() }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Start Recording")
                    }
                    if (state is RecordingState.Error) {
                        Spacer(Modifier.height(8.dp))
                        Text((state as RecordingState.Error).message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is RecordingState.Recording, is RecordingState.Paused, is RecordingState.Preparing, is RecordingState.Stopping -> {
                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.Red)
                    Spacer(Modifier.height(16.dp))
                    Text("● Recording", color = Color.Red, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(format(elapsed), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Triggered by: Volume Down ×3", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        if (state is RecordingState.Recording) {
                            OutlinedButton(onClick = { viewModel.pauseRecording() }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Pause, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Pause")
                            }
                        }
                        if (state is RecordingState.Paused) {
                            Button(onClick = { viewModel.resumeRecording() }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Resume")
                            }
                        }
                        Button(onClick = { viewModel.stopRecording() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Stop")
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Privacy Notice", fontWeight = FontWeight.Bold)
                    Text("Recording shows persistent notification and system indicator. No hidden recording.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
