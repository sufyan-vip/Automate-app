package com.buttonpilot.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.buttonpilot.app.HardwareShortcutDetector

class ButtonAccessibilityService : AccessibilityService() {

    private val detector = HardwareShortcutDetector()

    override fun onServiceConnected() {
        super.onServiceConnected()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return false
        if (event.action != KeyEvent.ACTION_DOWN) return false
        if (event.keyCode != KeyEvent.KEYCODE_VOLUME_DOWN && event.keyCode != KeyEvent.KEYCODE_VOLUME_UP) return false

        val detected = detector.onKeyDown(event.eventTime, event.keyCode, event.repeatCount)
        if (detected is HardwareShortcutDetector.Event.TripleVolumeDown) {
            val intent = Intent(this, AudioRecordingService::class.java).apply {
                action = AudioRecordingService.ACTION_TOGGLE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
            else startService(intent)
        }
        return false
    }
}
