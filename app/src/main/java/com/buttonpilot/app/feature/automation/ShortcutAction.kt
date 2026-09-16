package com.buttonpilot.app.feature.automation

sealed interface ShortcutAction {
    data object ToggleRecording : ShortcutAction
    data object ToggleFlashlight : ShortcutAction
    data class LaunchApp(val packageName: String) : ShortcutAction
    data object MediaPlayPause : ShortcutAction
    data object MediaNext : ShortcutAction
    data object MediaPrevious : ShortcutAction
    data object OpenCamera : ShortcutAction
    data object OpenRecorderLibrary : ShortcutAction
    data object ShowTime : ShortcutAction
    data object OpenCalculator : ShortcutAction
    data object OpenNotificationShade : ShortcutAction
    data object OpenQuickSettings : ShortcutAction
    data object AdjustMediaVolume : ShortcutAction
    data class OpenDialer(val number: String) : ShortcutAction
    data class OpenSmsComposer(val contact: String, val message: String) : ShortcutAction
    data object CreateNote : ShortcutAction
    data object StartTimer : ShortcutAction
    data object ToggleMute : ShortcutAction
    data object ToggleDnd : ShortcutAction

    fun displayName(): String = when (this) {
        is ToggleRecording -> "Toggle Voice Recording"
        is ToggleFlashlight -> "Flashlight Toggle"
        is LaunchApp -> "Launch App: $packageName"
        is MediaPlayPause -> "Media Play/Pause"
        is MediaNext -> "Next Track"
        is MediaPrevious -> "Previous Track"
        is OpenCamera -> "Open Camera"
        is OpenRecorderLibrary -> "Open Recorder Library"
        is ShowTime -> "Show Current Time"
        is OpenCalculator -> "Launch Calculator"
        is OpenNotificationShade -> "Open Notification Shade"
        is OpenQuickSettings -> "Open Quick Settings"
        is AdjustMediaVolume -> "Adjust Media Volume"
        is OpenDialer -> "Open Dialer: $number"
        is OpenSmsComposer -> "Open SMS: $contact"
        is CreateNote -> "Create Note"
        is StartTimer -> "Start Timer"
        is ToggleMute -> "Toggle Mute"
        is ToggleDnd -> "Toggle Do Not Disturb"
    }
}
