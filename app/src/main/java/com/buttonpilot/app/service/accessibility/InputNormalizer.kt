package com.buttonpilot.app.service.accessibility

import android.view.KeyEvent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Normalizes raw key events into KeySignal, filtering repeat and invalid events.
 */
@Singleton
class InputNormalizer @Inject constructor() {

    fun normalize(event: KeyEvent): KeySignal? {
        // Ignore repeat counts >0 for initial detection, but track for filtering
        if (event.repeatCount > 0) {
            // Filter accidental key-repeat events for triple detection
            return null
        }

        val action = when (event.action) {
            KeyEvent.ACTION_DOWN -> KeyAction.DOWN
            KeyEvent.ACTION_UP -> KeyAction.UP
            else -> return null
        }

        val keyCode = when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> KeyCode.VOLUME_DOWN
            KeyEvent.KEYCODE_VOLUME_UP -> KeyCode.VOLUME_UP
            else -> return null // Only care about volume keys for now
        }

        return KeySignal(
            keyCode = keyCode,
            action = action,
            eventTime = event.eventTime,
            repeatCount = event.repeatCount
        )
    }

    fun fromParams(keyCode: Int, action: Int, eventTime: Long, repeatCount: Int): KeySignal? {
        val mappedAction = when (action) {
            KeyEvent.ACTION_DOWN -> KeyAction.DOWN
            KeyEvent.ACTION_UP -> KeyAction.UP
            else -> return null
        }
        val mappedKey = when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> KeyCode.VOLUME_DOWN
            KeyEvent.KEYCODE_VOLUME_UP -> KeyCode.VOLUME_UP
            else -> return null
        }
        if (repeatCount > 0) return null
        return KeySignal(mappedKey, mappedAction, eventTime, repeatCount)
    }
}

enum class KeyCode {
    VOLUME_DOWN,
    VOLUME_UP
}

enum class KeyAction {
    DOWN,
    UP
}

data class KeySignal(
    val keyCode: KeyCode,
    val action: KeyAction,
    val eventTime: Long,
    val repeatCount: Int = 0
)
