package com.buttonpilot.app.feature.automation

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves conflicts like Volume Down x2 vs Volume Down x3.
 * Must delay execution where necessary to determine if another press is coming.
 * Uses finite-state-machine approach.
 */
@Singleton
class ShortcutConflictResolver @Inject constructor() {

    data class Config(
        val doublePressTimeout: Long = 450L,
        val triplePressTimeout: Long = 1100L
    )

    private var config = Config()
    private var pendingEvent: ShortcutEvent? = null
    private var pendingTime: Long = 0L

    fun updateConfig(newConfig: Config) {
        config = newConfig
    }

    /**
     * Resolve event, potentially delaying double-press to see if triple comes.
     * Returns event to dispatch immediately, or null if waiting for more presses.
     */
    fun resolve(event: ShortcutEvent, now: Long): ResolveResult {
        return when (event) {
            is ShortcutEvent.DoubleVolumeDown -> {
                // Hold double to see if triple arrives within timeout
                pendingEvent = event
                pendingTime = now
                ResolveResult.Pending(event)
            }
            is ShortcutEvent.TripleVolumeDown -> {
                // Triple overrides pending double
                pendingEvent = null
                ResolveResult.Dispatch(event)
            }
            is ShortcutEvent.DoubleVolumeUp -> {
                pendingEvent = event
                pendingTime = now
                ResolveResult.Pending(event)
            }
            is ShortcutEvent.TripleVolumeUp -> {
                pendingEvent = null
                ResolveResult.Dispatch(event)
            }
            else -> ResolveResult.Dispatch(event)
        }
    }

    /**
     * Called on timeout to flush pending events.
     */
    fun flushIfTimeout(now: Long): ShortcutEvent? {
        val pending = pendingEvent ?: return null
        val timeout = when (pending) {
            is ShortcutEvent.DoubleVolumeDown, is ShortcutEvent.DoubleVolumeUp -> config.doublePressTimeout
            else -> config.triplePressTimeout
        }
        return if (now - pendingTime >= timeout) {
            pendingEvent = null
            pending
        } else null
    }

    fun reset() {
        pendingEvent = null
        pendingTime = 0L
    }

    sealed class ResolveResult {
        data class Dispatch(val event: ShortcutEvent) : ResolveResult()
        data class Pending(val event: ShortcutEvent) : ResolveResult()
        data object Ignored : ResolveResult()
    }
}
