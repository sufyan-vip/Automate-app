package com.buttonpilot.app.feature.shortcuts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buttonpilot.app.feature.automation.ShortcutAction
import com.buttonpilot.app.feature.automation.TriggerKey
import com.buttonpilot.app.feature.automation.TriggerPattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutBuilderScreen(
    viewModel: ShortcutsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var selectedTriggerKey by remember { mutableStateOf(TriggerKey.VOLUME_DOWN) }
    var selectedPattern by remember { mutableStateOf<TriggerPattern>(TriggerPattern.TriplePress) }
    var selectedAction by remember { mutableStateOf<ShortcutAction>(ShortcutAction.ToggleRecording) }
    var timeWindow by remember { mutableStateOf(1100L) }
    var cooldown by remember { mutableStateOf(1500L) }
    var name by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Shortcut Builder") }, navigationIcon = {
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Visual Flow: Choose Trigger → Pattern → Action → Configure → Test → Save", style = MaterialTheme.typography.bodySmall)

            // Step 1: Choose Trigger
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Step 1: Choose Trigger", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = selectedTriggerKey == TriggerKey.VOLUME_DOWN, onClick = { selectedTriggerKey = TriggerKey.VOLUME_DOWN }, label = { Text("Volume Down") })
                        FilterChip(selected = selectedTriggerKey == TriggerKey.VOLUME_UP, onClick = { selectedTriggerKey = TriggerKey.VOLUME_UP }, label = { Text("Volume Up") })
                        FilterChip(selected = selectedTriggerKey == TriggerKey.VOLUME_BOTH, onClick = { selectedTriggerKey = TriggerKey.VOLUME_BOTH }, label = { Text("Both") })
                    }
                }
            }

            // Step 2: Choose Pattern
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Step 2: Choose Pattern", fontWeight = FontWeight.Bold)
                    val patterns = listOf(
                        TriggerPattern.SinglePress to "Single Press",
                        TriggerPattern.DoublePress to "Double Press",
                        TriggerPattern.TriplePress to "Triple Press",
                        TriggerPattern.LongPress to "Long Press",
                        TriggerPattern.BothVolumeButtons to "Both Volume Buttons"
                    )
                    patterns.forEach { (pattern, label) ->
                        FilterChip(selected = selectedPattern::class == pattern::class, onClick = { selectedPattern = pattern }, label = { Text(label) }, modifier = Modifier.padding(2.dp))
                    }
                }
            }

            // Step 3: Choose Action
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Step 3: Choose Action", fontWeight = FontWeight.Bold)
                    val actions = listOf(
                        ShortcutAction.ToggleRecording,
                        ShortcutAction.ToggleFlashlight,
                        ShortcutAction.MediaPlayPause,
                        ShortcutAction.OpenCamera,
                        ShortcutAction.OpenRecorderLibrary,
                        ShortcutAction.MediaNext,
                        ShortcutAction.MediaPrevious
                    )
                    actions.forEach { action ->
                        FilterChip(selected = selectedAction::class == action::class, onClick = { selectedAction = action }, label = { Text(action.displayName()) })
                    }
                }
            }

            // Step 4: Configure Options
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Step 4: Configure Options", fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Shortcut Name") }, modifier = Modifier.fillMaxWidth())
                    Text("Window: $timeWindow ms")
                    Slider(value = timeWindow.toFloat(), onValueChange = { timeWindow = it.toLong() }, valueRange = 500f..2000f)
                    Text("Cooldown: $cooldown ms")
                    Slider(value = cooldown.toFloat(), onValueChange = { cooldown = it.toLong() }, valueRange = 500f..3000f)
                    Text("Example:", fontWeight = FontWeight.Bold)
                    Text("Trigger: ${selectedTriggerKey.name}\nPattern: ${selectedPattern.displayName()}\nAction: ${selectedAction.displayName()}\nWindow: $timeWindow ms\nCooldown: $cooldown ms", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Existing shortcuts
            val shortcuts by viewModel.shortcuts.collectAsState()
            Text("Existing Shortcuts", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            shortcuts.forEach { sc ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(sc.name, fontWeight = FontWeight.Bold)
                        Text("${sc.triggerKey} - ${sc.triggerPattern} → ${sc.actionType}", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Switch(checked = sc.enabled, onCheckedChange = { viewModel.toggleEnabled(sc) })
                            Text(if (sc.enabled) "Enabled" else "Disabled")
                            Spacer(Modifier.weight(1f))
                            Button(onClick = { viewModel.delete(sc) }) { Text("Delete") }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val finalName = if (name.isBlank()) "${selectedTriggerKey.name} ${selectedPattern.displayName()} → ${selectedAction.displayName()}" else name
                    viewModel.createShortcut(finalName, selectedTriggerKey, selectedPattern, selectedAction)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Shortcut")
            }
        }
    }
}
