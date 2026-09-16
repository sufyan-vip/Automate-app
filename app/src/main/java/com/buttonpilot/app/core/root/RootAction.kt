package com.buttonpilot.app.core.root

/**
 * Strict allow-list for root actions. No arbitrary shell execution.
 */
enum class RootAction {
    CheckRoot,
    StartInputMonitor,
    StopInputMonitor,
    ListInputDevices,
    DumpInputEvents,
    RestartAccessibility
}
