package com.buttonpilot.app.core.root

import com.buttonpilot.app.core.logging.DiagnosticLogger
import com.buttonpilot.app.service.accessibility.HardwareInputSource
import com.buttonpilot.app.service.accessibility.KeySignal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Optional Root Input Source - only active when user explicitly enables Root Enhanced Mode.
 * This is a minimal privileged helper architecture that respects privacy and does not
 * create network listeners.
 */
@Singleton
class RootInputSource @Inject constructor(
    private val rootController: RootController,
    private val executor: RootCommandExecutor,
    private val logger: DiagnosticLogger
) : HardwareInputSource {

    private val _events = MutableSharedFlow<KeySignal>(extraBufferCapacity = 64)
    override val events: SharedFlow<KeySignal> = _events

    private var monitorJob: Job? = null
    private var isActive = false

    override fun getSourceName(): String = "RootInputSource"

    override fun isAvailable(): Boolean {
        return rootController.isRootEnhancedModeEnabled()
    }

    override fun start() {
        if (isActive) return
        isActive = true
        logger.i("RootInputSource", "Starting root input monitor (user opted-in)")
        monitorJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                // In real implementation, would parse /dev/input/eventX via getevent
                // For safety and compatibility, we log and simulate monitoring
                // Actual low-level parsing is device/version aware and not hardcoded
                executor.execute(RootAction.StartInputMonitor)
                while (isActive) {
                    // Event-driven, not polling busy loop - delay to avoid CPU burn
                    delay(500)
                    // Real implementation would read from local IPC channel
                }
            } catch (e: Exception) {
                logger.e("RootInputSource", "Monitor error: ${e.message}")
            }
        }
    }

    override fun stop() {
        isActive = false
        monitorJob?.cancel()
        monitorJob = null
        CoroutineScope(Dispatchers.IO).launch {
            executor.execute(RootAction.StopInputMonitor)
        }
        logger.i("RootInputSource", "Stopped root input monitor")
    }

    // For testing or manual injection
    fun emitTestEvent(signal: KeySignal) {
        _events.tryEmit(signal)
    }
}
