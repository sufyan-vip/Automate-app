package com.buttonpilot.app.feature.automation

import com.buttonpilot.app.core.logging.DiagnosticLogger
import com.buttonpilot.app.service.accessibility.InputNormalizer
import com.buttonpilot.app.service.accessibility.KeySignal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central event pipeline:
 * Raw Key Event -> HardwareShortcutDetector -> Dispatcher
 *
 * Double press is delayed by the detector until maxIntervalBetweenPresses elapses
 * so a third press forming a triple wins.
 */
@Singleton
class ShortcutEventBus @Inject constructor(
    private val detector: HardwareShortcutDetector,
    private val conflictResolver: ShortcutConflictResolver,
    private val dispatcher: ShortcutActionDispatcher,
    private val logger: DiagnosticLogger
) {
    private val _rawEvents = MutableSharedFlow<KeySignal>(extraBufferCapacity = 64)
    private val _shortcutEvents = MutableSharedFlow<ShortcutEvent>(extraBufferCapacity = 32)
    private val _dispatchedResults = MutableSharedFlow<ShortcutActionDispatcher.DispatchResult>(extraBufferCapacity = 32)

    val shortcutEvents: SharedFlow<ShortcutEvent> = _shortcutEvents.asSharedFlow()
    val dispatchedResults: SharedFlow<ShortcutActionDispatcher.DispatchResult> = _dispatchedResults.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.Default)
    private var flushJob: Job? = null

    init {
        startPipeline()
    }

    private fun startPipeline() {
        scope.launch {
            _rawEvents.collect { signal ->
                try {
                    val detected = detector.onKeySignal(signal)
                    if (detected != null) {
                        flushJob?.cancel()
                        dispatch(detected)
                    } else {
                        // After each keystroke, schedule a check so a pending double-press
                        // is emitted after the triple-press window passes.
                        scheduleFlush()
                    }
                } catch (e: Exception) {
                    logger.e("EventBus", "Pipeline error: ${e.message}", e)
                }
            }
        }
    }

    private suspend fun dispatch(event: ShortcutEvent) {
        logger.i("EventBus", "Detected: $event")
        _shortcutEvents.emit(event)
        val result = dispatcher.dispatch(event)
        _dispatchedResults.emit(result)
    }

    private fun scheduleFlush() {
        flushJob?.cancel()
        flushJob = scope.launch {
            delay(500L)
            val pending = detector.flushPending(System.currentTimeMillis())
            if (pending != null) {
                dispatch(pending)
            }
        }
    }

    fun emitRaw(signal: KeySignal) {
        _rawEvents.tryEmit(signal)
    }

    fun reset() {
        detector.reset()
        conflictResolver.reset()
        flushJob?.cancel()
    }
}
