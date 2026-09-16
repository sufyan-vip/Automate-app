package com.buttonpilot.app.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.buttonpilot.app.core.logging.DiagnosticLogger
import com.buttonpilot.app.feature.automation.ShortcutEventBus
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ButtonAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var inputNormalizer: InputNormalizer

    @Inject
    lateinit var eventBus: ShortcutEventBus

    @Inject
    lateinit var logger: DiagnosticLogger

    @Inject
    lateinit var accessibilityInputSource: AccessibilityInputSource

    override fun onServiceConnected() {
        super.onServiceConnected()
        logger.i("ButtonAccessibilityService", "Service connected - hardware key monitoring active")
        accessibilityInputSource.start()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We avoid retrieving window content unless another explicit feature requires it
        // This service is lightweight and focused on hardware keys
    }

    override fun onInterrupt() {
        logger.w("ButtonAccessibilityService", "Service interrupted")
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return false

        // Only handle volume keys
        if (event.keyCode != KeyEvent.KEYCODE_VOLUME_DOWN && event.keyCode != KeyEvent.KEYCODE_VOLUME_UP) {
            return false
        }

        logger.d("ButtonAccessibilityService", "Key event: ${event.keyCode} action=${event.action} time=${event.eventTime} repeat=${event.repeatCount}")

        val signal = inputNormalizer.normalize(event) ?: return false

        // Send normalized events to HardwareShortcutDetector via event bus
        eventBus.emitRaw(signal)
        accessibilityInputSource.emitEvent(signal)

        // Important: Where Android does not allow suppressing original system behavior reliably, do not fake it
        // Return false to allow system to still handle volume change, unless user configured to consume
        // For now, we return false to avoid breaking volume UX
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        accessibilityInputSource.stop()
        logger.i("ButtonAccessibilityService", "Service destroyed")
    }
}
