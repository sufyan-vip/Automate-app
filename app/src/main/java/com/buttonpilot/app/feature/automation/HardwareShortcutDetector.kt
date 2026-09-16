package com.buttonpilot.app.feature.automation

import com.buttonpilot.app.service.accessibility.KeyAction
import com.buttonpilot.app.service.accessibility.KeyCode
import com.buttonpilot.app.service.accessibility.KeySignal
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extremely reliable state machine for hardware button pattern detection.
 * Testable, no Android dependencies, processes only KEY_DOWN.
 */
@Singleton
class HardwareShortcutDetector @Inject constructor() {

    data class Config(
        val maxIntervalBetweenPressesMs: Long = 450L,
        val sequenceTimeoutMs: Long = 1100L,
        val cooldownMs: Long = 1500L
    )

    private var config = Config()
    private val pressTimes = mutableListOf<Long>()
    private var lastTriggerTime: Long = 0L
    private var lastEventTime: Long = 0L

    // For volume up tracking as well
    private val pressTimesUp = mutableListOf<Long>()

    fun updateConfig(newConfig: Config) {
        config = newConfig
    }

    /**
     * Process a normalized key signal and return detected shortcut event if any.
     */
    fun onKeySignal(signal: KeySignal): ShortcutEvent? {
        // Process KEY_DOWN only when appropriate, ignore KEY_UP for triple detection
        if (signal.action != KeyAction.DOWN) return null

        val now = signal.eventTime

        // Check cooldown to prevent single triple press firing twice
        if (now - lastTriggerTime < config.cooldownMs) {
            return null
        }

        // Reset if timeout exceeded
        if (lastEventTime != 0L && now - lastEventTime > config.sequenceTimeoutMs) {
            pressTimes.clear()
            pressTimesUp.clear()
        }

        lastEventTime = now

        return when (signal.keyCode) {
            KeyCode.VOLUME_DOWN -> handleVolumeDown(now)
            KeyCode.VOLUME_UP -> handleVolumeUp(now)
        }
    }

    private fun handleVolumeDown(now: Long): ShortcutEvent? {
        // Clean old presses beyond sequence timeout
        pressTimes.removeAll { now - it > config.sequenceTimeoutMs }

        // Check interval between consecutive presses
        if (pressTimes.isNotEmpty()) {
            val lastPress = pressTimes.last()
            if (now - lastPress > config.maxIntervalBetweenPressesMs) {
                pressTimes.clear()
            }
        }

        pressTimes.add(now)

        // Detect patterns
        return when (pressTimes.size) {
            2 -> {
                // Check if it's within interval
                if (pressTimes[1] - pressTimes[0] <= config.maxIntervalBetweenPressesMs) {
                    ShortcutEvent.DoubleVolumeDown
                } else {
                    pressTimes.clear()
                    pressTimes.add(now)
                    null
                }
            }
            3 -> {
                val interval1 = pressTimes[1] - pressTimes[0]
                val interval2 = pressTimes[2] - pressTimes[1]
                val total = pressTimes[2] - pressTimes[0]
                if (interval1 <= config.maxIntervalBetweenPressesMs &&
                    interval2 <= config.maxIntervalBetweenPressesMs &&
                    total <= config.sequenceTimeoutMs
                ) {
                    // Triple detected
                    lastTriggerTime = now
                    pressTimes.clear()
                    ShortcutEvent.TripleVolumeDown
                } else {
                    // Shift window: keep last 2
                    val lastTwo = pressTimes.takeLast(2).toMutableList()
                    pressTimes.clear()
                    pressTimes.addAll(lastTwo)
                    // Recheck if last two still form double?
                    null
                }
            }
            else -> {
                if (pressTimes.size > 3) {
                    // Keep only last 3 for safety
                    val lastThree = pressTimes.takeLast(3)
                    pressTimes.clear()
                    pressTimes.addAll(lastThree)
                }
                null
            }
        }
    }

    private fun handleVolumeUp(now: Long): ShortcutEvent? {
        pressTimesUp.removeAll { now - it > config.sequenceTimeoutMs }

        if (pressTimesUp.isNotEmpty()) {
            val lastPress = pressTimesUp.last()
            if (now - lastPress > config.maxIntervalBetweenPressesMs) {
                pressTimesUp.clear()
            }
        }

        pressTimesUp.add(now)

        return when (pressTimesUp.size) {
            2 -> {
                if (pressTimesUp[1] - pressTimesUp[0] <= config.maxIntervalBetweenPressesMs) {
                    ShortcutEvent.DoubleVolumeUp
                } else {
                    pressTimesUp.clear()
                    pressTimesUp.add(now)
                    null
                }
            }
            3 -> {
                val i1 = pressTimesUp[1] - pressTimesUp[0]
                val i2 = pressTimesUp[2] - pressTimesUp[1]
                val total = pressTimesUp[2] - pressTimesUp[0]
                if (i1 <= config.maxIntervalBetweenPressesMs && i2 <= config.maxIntervalBetweenPressesMs && total <= config.sequenceTimeoutMs) {
                    lastTriggerTime = now
                    pressTimesUp.clear()
                    ShortcutEvent.TripleVolumeUp
                } else {
                    val lastTwo = pressTimesUp.takeLast(2).toMutableList()
                    pressTimesUp.clear()
                    pressTimesUp.addAll(lastTwo)
                    null
                }
            }
            else -> null
        }
    }

    fun reset() {
        pressTimes.clear()
        pressTimesUp.clear()
        lastEventTime = 0L
    }

    fun getCurrentState(): DetectorState {
        return DetectorState(
            downPressCount = pressTimes.size,
            upPressCount = pressTimesUp.size,
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
