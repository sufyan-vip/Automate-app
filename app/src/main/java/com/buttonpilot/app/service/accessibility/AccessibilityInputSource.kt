package com.buttonpilot.app.service.accessibility

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccessibilityInputSource @Inject constructor() : HardwareInputSource {

    private val _events = MutableSharedFlow<KeySignal>(extraBufferCapacity = 64)
    override val events: SharedFlow<KeySignal> = _events

    private var active = false

    override fun getSourceName(): String = "AccessibilityInputSource"

    override fun isAvailable(): Boolean = true // Availability checked via PermissionManager

    override fun start() {
        active = true
    }

    override fun stop() {
        active = false
    }

    fun emitEvent(signal: KeySignal) {
        if (active) {
            _events.tryEmit(signal)
        }
    }
}
