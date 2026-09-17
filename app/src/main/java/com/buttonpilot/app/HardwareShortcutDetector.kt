package com.buttonpilot.app

class HardwareShortcutDetector(
    private val maxIntervalMs: Long = 450L,
    private val sequenceTimeoutMs: Long = 1100L,
    private val cooldownMs: Long = 1500L
) {
    sealed class Event {
        data object TripleVolumeDown : Event()
    }

    companion object {
        const val KEYCODE_VOLUME_DOWN = 25
        const val KEYCODE_VOLUME_UP = 24
    }

    private val presses = mutableListOf<Long>()
    private var lastTriggerTime: Long = 0L
    private var lastEventTime: Long = 0L

    fun onKeyDown(eventTime: Long, keyCode: Int, repeatCount: Int): Event? {
        if (repeatCount > 0) return null
        if (keyCode != KEYCODE_VOLUME_DOWN && keyCode != KEYCODE_VOLUME_UP) return null
        if (eventTime - lastTriggerTime < cooldownMs) return null

        if (lastEventTime != 0L && eventTime - lastEventTime > sequenceTimeoutMs) {
            presses.clear()
        }
        lastEventTime = eventTime

        presses.removeAll { eventTime - it > sequenceTimeoutMs }
        if (presses.isNotEmpty() && eventTime - presses.last() > maxIntervalMs) presses.clear()
        presses.add(eventTime)

        if (presses.size >= 3 && keyCode == KEYCODE_VOLUME_DOWN) {
            val a = presses[presses.size - 3]
            val b = presses[presses.size - 2]
            val c = presses[presses.size - 1]
            if (b - a <= maxIntervalMs && c - b <= maxIntervalMs && c - a <= sequenceTimeoutMs) {
                lastTriggerTime = eventTime
                presses.clear()
                return Event.TripleVolumeDown
            }
        }
        return null
    }

    fun reset() {
        presses.clear()
        lastEventTime = 0L
    }
}
