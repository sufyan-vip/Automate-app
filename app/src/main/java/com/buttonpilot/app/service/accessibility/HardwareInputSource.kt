package com.buttonpilot.app.service.accessibility

import kotlinx.coroutines.flow.SharedFlow

/**
 * Compatibility abstraction for hardware input sources.
 * Only one source should be active at a time unless carefully deduplicated.
 */
interface HardwareInputSource {
    val events: SharedFlow<KeySignal>
    fun getSourceName(): String
    fun isAvailable(): Boolean
    fun start()
    fun stop()
}
