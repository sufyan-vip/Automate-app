package com.buttonpilot.app.feature.settings

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buttonpilot.app.core.storage.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val preferencesManager: PreferencesManager
) : ViewModel() {
    var doubleInterval by mutableStateOf(450L)
    var tripleInterval by mutableStateOf(450L)
    var sequenceTimeout by mutableStateOf(1100L)
    var longPress by mutableStateOf(600L)
    var cooldown by mutableStateOf(1500L)
    var vibration by mutableStateOf(true)
    var haptic by mutableStateOf(true)
    var rootEnhanced by mutableStateOf(false)
    var shortcutsEnabled by mutableStateOf(true)
    var audioQuality by mutableStateOf("HIGH")

    init {
        viewModelScope.launch {
            preferencesManager.doublePressInterval.collectLatest { doubleInterval = it }
        }
        viewModelScope.launch {
            preferencesManager.triplePressInterval.collectLatest { tripleInterval = it }
        }
        viewModelScope.launch {
            preferencesManager.sequenceTimeout.collectLatest { sequenceTimeout = it }
        }
        viewModelScope.launch {
            preferencesManager.longPressDuration.collectLatest { longPress = it }
        }
        viewModelScope.launch {
            preferencesManager.cooldown.collectLatest { cooldown = it }
        }
        viewModelScope.launch {
            preferencesManager.vibrationEnabled.collectLatest { vibration = it }
        }
        viewModelScope.launch {
            preferencesManager.hapticEnabled.collectLatest { haptic = it }
        }
        viewModelScope.launch {
            preferencesManager.rootEnhancedEnabled.collectLatest { rootEnhanced = it }
        }
        viewModelScope.launch {
            preferencesManager.shortcutsEnabled.collectLatest { shortcutsEnabled = it }
        }
        viewModelScope.launch {
            preferencesManager.audioQuality.collectLatest { audioQuality = it }
        }
    }

    fun setDouble(v: Long) = viewModelScope.launch { preferencesManager.setDoublePressInterval(v) }
    fun setTriple(v: Long) = viewModelScope.launch { preferencesManager.setTriplePressInterval(v) }
    fun setSequence(v: Long) = viewModelScope.launch { preferencesManager.setSequenceTimeout(v) }
    fun setLongPress(v: Long) = viewModelScope.launch { preferencesManager.setLongPressDuration(v) }
    fun setCooldown(v: Long) = viewModelScope.launch { preferencesManager.setCooldown(v) }
    fun setVibration(v: Boolean) = viewModelScope.launch { preferencesManager.setVibrationEnabled(v) }
    fun setHaptic(v: Boolean) = viewModelScope.launch { preferencesManager.setHapticEnabled(v) }
    fun setRootEnhanced(v: Boolean) = viewModelScope.launch { preferencesManager.setRootEnhancedEnabled(v) }
    fun setShortcutsEnabled(v: Boolean) = viewModelScope.launch { preferencesManager.setShortcutsEnabled(v) }
    fun setAudioQuality(v: String) = viewModelScope.launch { preferencesManager.setAudioQuality(v) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") }, navigationIcon = {
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
            Text("Detection", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            SettingSlider("Double press interval", viewModel.doubleInterval, 200L..800L, { viewModel.setDouble(it) })
            SettingSlider("Triple press interval", viewModel.tripleInterval, 200L..800L, { viewModel.setTriple(it) })
            SettingSlider("Sequence timeout", viewModel.sequenceTimeout, 500L..2000L, { viewModel.setSequence(it) })
            SettingSlider("Long press duration", viewModel.longPress, 300L..1500L, { viewModel.setLongPress(it) })
            SettingSlider("Trigger cooldown", viewModel.cooldown, 500L..3000L, { viewModel.setCooldown(it) })

            Divider()

            Text("Recorder", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Vibration feedback")
                Switch(checked = viewModel.vibration, onCheckedChange = { viewModel.setVibration(it) })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Shortcut Vibration")
                Switch(checked = viewModel.haptic, onCheckedChange = { viewModel.setHaptic(it) })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Shortcuts Enabled")
                Switch(checked = viewModel.shortcutsEnabled, onCheckedChange = { viewModel.setShortcutsEnabled(it) })
            }

            Divider()
            Text("Background", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Battery guidance: Disable battery optimization for ButtonPilot in system settings if OEM kills services.", style = MaterialTheme.typography.bodySmall)

            Divider()
            Text("Advanced", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Root Enhanced Mode", fontWeight = FontWeight.Bold)
                    Text("Optional for device owner rooted device. Isolated behind interface.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = viewModel.rootEnhanced, onCheckedChange = { viewModel.setRootEnhanced(it) })
            }
        }
    }
}

@Composable
fun SettingSlider(name: String, value: Long, range: LongRange, onChange: (Long) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name)
            Text("${value} ms")
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toLong()) },
            valueRange = range.first.toFloat()..range.last.toFloat()
        )
    }
}
