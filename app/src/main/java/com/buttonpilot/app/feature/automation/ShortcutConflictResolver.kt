package com.buttonpilot.app.feature.automation

import javax.inject.Inject
import javax.inject.Singleton

/**
 * This resolver is now intentionally thin: the real conflict resolution lives in
 * [HardwareShortcutDetector] (double is never emitted early; triple wins), so no
 * additional buffering is needed here. The class is retained to provide a single
 * extension point if future sequence types (long press, both buttons, etc.) need it.
 */
@Singleton
class ShortcutConflictResolver @Inject constructor() {

    data class Config(
        val doublePressTimeout: Long = 450L,
        val triplePressTimeout: Long = 1100L
    )

    private var config = Config()

    fun updateConfig(newConfig: Config) {
        config = newConfig
    }

    fun resolve(event: ShortcutEvent, @Suppress("UNUSED_PARAMETER") now: Long): ResolveResult {
        return ResolveResult.Dispatch(event)
    }

    fun reset() = Unit

    sealed class ResolveResult {
        data class Dispatch(val event: ShortcutEvent) : ResolveResult()
        data class Pending(val event: ShortcutEvent) : ResolveResult()
        data object Ignored : ResolveResult()
    }
}
