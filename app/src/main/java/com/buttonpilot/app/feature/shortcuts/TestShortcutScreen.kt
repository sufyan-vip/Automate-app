package com.buttonpilot.app.feature.shortcuts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buttonpilot.app.core.logging.DiagnosticLogger
import com.buttonpilot.app.feature.automation.HardwareShortcutDetector
import com.buttonpilot.app.feature.automation.ShortcutEvent
import com.buttonpilot.app.feature.automation.ShortcutEventBus
import com.buttonpilot.app.service.accessibility.KeySignal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestShortcutViewModel @Inject constructor(
    val eventBus: ShortcutEventBus,
    val detector: HardwareShortcutDetector,
    val logger: DiagnosticLogger
) : ViewModel() {
    var events by mutableStateOf<List<String>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            eventBus.shortcutEvents.collectLatest { event ->
                val msg = "${System.currentTimeMillis()}: $event"
                events = (listOf(msg) + events).take(20)
            }
        }
    }

    fun addTestEvent() {
        val signal = com.buttonpilot.app.service.accessibility.KeySignal(
            keyCode = com.buttonpilot.app.service.accessibility.KeyCode.VOLUME_DOWN,
            action = com.buttonpilot.app.service.accessibility.KeyAction.DOWN,
            eventTime = System.currentTimeMillis(),
            repeatCount = 0
        )
        eventBus.emitRaw(signal)
    }

    fun reset() {
        eventBus.reset()
        events = emptyList()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestShortcutScreen(
    viewModel: TestShortcutViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Test Shortcut") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Real-time recognized button events", fontWeight = FontWeight.Bold)
            Text("Press Volume Down 3 times rapidly to test. This screen shows detector output.", style = MaterialTheme.typography.bodySmall)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val state = viewModel.detector.getCurrentState()
                    Text("Detector State", fontWeight = FontWeight.Bold)
                    Text("Down count: ${state.downPressCount}")
                    Text("Up count: ${state.upPressCount}")
                    Text("Last event: ${state.lastEventTime}")
                    Text("Cooldown: ${state.isInCooldown}")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { viewModel.addTestEvent() }, modifier = Modifier.weight(1f)) {
                    Text("Simulate Volume Down")
                }
                OutlinedButton(onClick = { viewModel.reset() }, modifier = Modifier.weight(1f)) {
                    Text("Reset")
                }
            }

            Text("Last 20 shortcut events:", fontWeight = FontWeight.Bold)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxSize()) {
                items(viewModel.events) { event ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(event, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
