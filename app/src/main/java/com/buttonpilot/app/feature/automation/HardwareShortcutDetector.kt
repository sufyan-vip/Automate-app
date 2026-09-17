package com.buttonpilot.app.feature.automation

import com.buttonpilot.app.service.accessibility.KeyAction
import com.buttonpilot.app.service.accessibility.KeyCode
import com.buttonpilot.app.service.accessibility.KeySignal
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extremely reliable state machine for hardware button pattern detection.
 * Testable, no Android dependencies, processes only KEY_DOWN.
 *
 * Implements conflict resolution: a double-press is held until maxIntervalBetweenPresses
 * elapses so a third press (making it a triple) is not shadowed by a premature double.
 */
@Singleton
class HardwareShortcutDetector @Inject constructor() {

    data class Config(
        val maxIntervalBetweenPressesMs: Long = 450L,
        val sequenceTimeoutMs: Long = 1100L,
        val cooldownMs: Long = 1500L
    )

    private var config = Config()

    // For each key, we track the timestamps of presses in the current sequence.
    private val downPresses = mutableListOf<Long>()
    private val upPresses = mutableListOf<Long>()

    private var lastTriggerTime: Long = 0L
    private var lastEventTime: Long = 0L

    fun updateConfig(newConfig: Config) {
        config = newConfig
    }

    /**
     * Process a normalized key signal and return detected shortcut event if any.
     */
    fun onKeySignal(signal: KeySignal): ShortcutEvent? {
        if (signal.action != KeyAction.DOWN) return null

        val now = signal.eventTime

        // Cooldown prevents a single triple press firing twice
        if (now - lastTriggerTime < config.cooldownMs) {
            return null
        }

        // Reset sequence if gap since last event exceeds the timeout
        if (lastEventTime != 0L && now - lastEventTime > config.sequenceTimeoutMs) {
            downPresses.clear()
            upPresses.clear()
        }
        lastEventTime = now

        return when (signal.keyCode) {
            KeyCode.VOLUME_DOWN -> handle(downPresses, now, isDown = true)
            KeyCode.VOLUME_UP -> handle(upPresses, now, isDown = false)
        }
    }

    private fun handle(presses: MutableList<Long>, now: Long, isDown: Boolean): ShortcutEvent? {
        // Drop presses older than sequenceTimeout
        presses.removeAll { now - it > config.sequenceTimeoutMs }

        // If the gap from previous press exceeds max interval, restart sequence
        if (presses.isNotEmpty() && now - presses.last() > config.maxIntervalBetweenPressesMs) {
            presses.clear()
        }

        presses.add(now)

        // Emit triple on 3rd press. Do NOT emit double on 2nd press; let the conflict
        // resolver / timeout flush a pending double after the waiting window passes.
        if (presses.size >= 3) {
            val a = presses[presses.size - 3]
            val b = presses[presses.size - 2]
            val c = presses[presses.size - 1]
            if (b - a <= config.maxIntervalBetweenPressesMs &&
                c - b <= config.maxIntervalBetweenPressesMs &&
                c - a <= config.sequenceTimeoutMs
            ) {
                lastTriggerTime = now
                presses.clear()
                return if (isDown) ShortcutEvent.TripleVolumeDown else ShortcutEvent.TripleVolumeUp
            } else {
                // Shift window: keep only the last two presses for re-evaluation
                val lastTwo = presses.takeLast(2).toMutableList()
                presses.clear()
                presses.addAll(lastTwo)
            }
        }
        return null
    }

    /** Called by [ShortcutEventBus] when the pending-double timeout fires. */
    fun flushPending(now: Long): ShortcutEvent? {
        val results = mutableListOf<ShortcutEvent>()
        if (shouldFlushDouble(downPresses, now)) {
            downPresses.clear()
            results += ShortcutEvent.DoubleVolumeDown
        }
        if (shouldFlushDouble(upPresses, now)) {
            upPresses.clear()
            results += ShortcutEvent.DoubleVolumeUp
        }
        // Only one flush expected at a time - return the first
        return results.firstOrNull()
    }

    private fun shouldFlushDouble(presses: MutableList<Long>, now: Long): Boolean {
        if (presses.size == 2) {
            val (a, b) = presses[0] to presses[1]
            if (b - a <= config.maxIntervalBetweenPressesMs &&
                now - b >= config.maxIntervalBetweenPressesMs
            ) {
                return true
            }
        }
        return false
    }

    fun reset() {
        downPresses.clear()
        upPresses.clear()
        lastEventTime = 0L
        lastTriggerTime = 0L
    }

    fun getCurrentState(): DetectorState {
        return DetectorState(
            downPressCount = downPresses.size,
            upPressCount = upPresses.size,
            lastEventTime = lastEventTime,
            lastTriggerTime = lastTriggerTime,
            isInCooldown = System.currentTimeMillis() - lastTriggerTime < config.cooldownMs
        )
    }
}

data class DetectorState(
    val downPressCount: Int,
    val upPressCount: Int,
    val lastEventTime: Long,
    val lastTriggerTime: Long,
    val isInCooldown: Boolean
)

sealed class ShortcutEvent {
    data object TripleVolumeDown : ShortcutEvent()
    data object DoubleVolumeDown : ShortcutEvent()
    data object TripleVolumeUp : ShortcutEvent()
    data object DoubleVolumeUp : ShortcutEvent()
    data object VolumeUpThenDown : ShortcutEvent()
    data object VolumeDownThenUp : ShortcutEvent()
    data object BothVolumeButtons : ShortcutEvent()
    data object SingleVolumeDown : ShortcutEvent()
    data object SingleVolumeUp : ShortcutEvent()
}
