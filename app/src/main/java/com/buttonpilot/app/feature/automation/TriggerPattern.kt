package com.buttonpilot.app.feature.automation

sealed class TriggerPattern {
    data object SinglePress : TriggerPattern()
    data object DoublePress : TriggerPattern()
    data object TriplePress : TriggerPattern()
    data object LongPress : TriggerPattern()
    data object PressAndHold : TriggerPattern()
    data class VolumeSequence(val sequence: List<String>) : TriggerPattern()
    data object BothVolumeButtons : TriggerPattern()

    fun displayName(): String = when (this) {
        SinglePress -> "Single Press"
        DoublePress -> "Double Press"
        TriplePress -> "Triple Press"
        LongPress -> "Long Press"
        PressAndHold -> "Press and Hold"
        is VolumeSequence -> sequence.joinToString(" → ")
        BothVolumeButtons -> "Both Volume Buttons"
    }
}

enum class TriggerKey {
    VOLUME_DOWN,
    VOLUME_UP,
    VOLUME_BOTH
}

data class ShortcutRule(
    val id: Long = 0,
    val name: String,
    val triggerKey: TriggerKey,
    val trigger: TriggerPattern,
    val action: ShortcutAction,
    val enabled: Boolean = true,
    val timeWindowMs: Long = 1100L,
    val cooldownMs: Long = 1500L,
    val consumeEvent: Boolean = false
)
