package com.buttonpilot.app.feature.recorder

import android.content.Intent
import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buttonpilot.app.data.local.AppDatabase
import com.buttonpilot.app.data.local.RecordingEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class RecordingsListViewModel @Inject constructor(
    private val database: AppDatabase
) : ViewModel() {
    val recordings = database.recordingDao().getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(recording: RecordingEntity) {
        viewModelScope.launch {
            try {
                File(recording.path).delete()
            } catch (e: Exception) {}
            database.recordingDao().delete(recording)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingsListScreen(
    viewModel: RecordingsListViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val recordings by viewModel.recordings.collectAsState()
    val context = LocalContext.current
    var playingId by remember { mutableStateOf<Long?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Recordings") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
            })
        }
    ) { padding ->
        if (recordings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No recordings yet. Triple-press Volume Down to start.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(recordings) { rec ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(rec.filename, style = MaterialTheme.typography.titleMedium)
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                            Text("Date: ${sdf.format(Date(rec.createdAt))}", style = MaterialTheme.typography.bodySmall)
                            Text("Duration: ${rec.duration / 1000}s | Size: ${rec.size / 1024} KB", style = MaterialTheme.typography.bodySmall)
                            Text("Source: ${rec.sourceShortcut}", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = {
                                    try {
                                        mediaPlayer?.release()
                                        if (playingId == rec.id) {
                                            playingId = null
                                        } else {
                                            val mp = MediaPlayer().apply {
                                                setDataSource(rec.path)
                                                prepare()
                                                start()
                                            }
                                            mediaPlayer = mp
                                            playingId = rec.id
                                            mp.setOnCompletionListener {
                                                playingId = null
                                            }
                                        }
                                    } catch (e: Exception) {}
                                }) {
                                    Icon(if (playingId == rec.id) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play")
                                }
                                IconButton(onClick = {
                                    try {
                                        val file = File(rec.path)
                                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                        val share = Intent(Intent.ACTION_SEND).apply {
                                            type = "audio/*"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(share, "Share recording"))
                                    } catch (e: Exception) {
                                        // fallback: open with external
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(android.net.Uri.parse(rec.path), "audio/*")
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        try { context.startActivity(intent) } catch (ex: Exception) {}
                                    }
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share")
                                }
                                IconButton(onClick = { viewModel.delete(rec) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }
}
