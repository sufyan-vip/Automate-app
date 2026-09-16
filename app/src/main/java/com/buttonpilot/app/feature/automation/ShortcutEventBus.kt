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
 * Raw Key Event → Input Normalizer → Shortcut Detector → Conflict Resolver → Action Dispatcher → Action Handler
 */
@Singleton
class ShortcutEventBus @Inject constructor(
    private val normalizer: InputNormalizer,
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
                        logger.i("EventBus", "Detected: $detected from $signal")
                        val now = System.currentTimeMillis()
                        val resolved = conflictResolver.resolve(detected, now)
                        when (resolved) {
                            is ShortcutConflictResolver.ResolveResult.Dispatch -> {
                                _shortcutEvents.emit(resolved.event)
                                val result = dispatcher.dispatch(resolved.event)
                                _dispatchedResults.emit(result)
                            }
                            is ShortcutConflictResolver.ResolveResult.Pending -> {
                                logger.d("EventBus", "Pending: ${resolved.event}, waiting for potential triple")
                                _shortcutEvents.emit(resolved.event) // For diagnostics UI
                                scheduleFlush()
                            }
                            is ShortcutConflictResolver.ResolveResult.Ignored -> {
                                logger.d("EventBus", "Ignored: $detected")
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.e("EventBus", "Pipeline error: ${e.message}")
                }
            }
        }
    }

    private fun scheduleFlush() {
        flushJob?.cancel()
        flushJob = scope.launch {
            delay(500)
            val now = System.currentTimeMillis()
            val pending = conflictResolver.flushIfTimeout(now)
            if (pending != null) {
                logger.i("EventBus", "Flushing pending: $pending")
                _shortcutEvents.emit(pending)
                val result = dispatcher.dispatch(pending)
                _dispatchedResults.emit(result)
            }
        }
    }

    fun emitRaw(signal: KeySignal) {
        _rawEvents.tryEmit(signal)
    }

    fun reset() {
        detector.reset()
        conflictResolver.reset()
    }
}
