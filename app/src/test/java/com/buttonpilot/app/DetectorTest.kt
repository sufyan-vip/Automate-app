package com.buttonpilot.app

import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DetectorTest {
    @Test
    fun tripleVolumeDownDetected() {
        val d = HardwareShortcutDetector()
        assertNull(d.onKeyDown(0, HardwareShortcutDetector.KEYCODE_VOLUME_DOWN, 0))
        assertNull(d.onKeyDown(270, HardwareShortcutDetector.KEYCODE_VOLUME_DOWN, 0))
        val r = d.onKeyDown(520, HardwareShortcutDetector.KEYCODE_VOLUME_DOWN, 0)
        assertTrue(r is HardwareShortcutDetector.Event.TripleVolumeDown)
    }

    @Test
    fun singlePressNoTrigger() {
        val d = HardwareShortcutDetector()
        assertNull(d.onKeyDown(0, HardwareShortcutDetector.KEYCODE_VOLUME_DOWN, 0))
    }

    @Test
    fun timeoutResets() {
        val d = HardwareShortcutDetector()
        d.onKeyDown(0, HardwareShortcutDetector.KEYCODE_VOLUME_DOWN, 0)
        d.onKeyDown(300, HardwareShortcutDetector.KEYCODE_VOLUME_DOWN, 0)
        val r = d.onKeyDown(2000, HardwareShortcutDetector.KEYCODE_VOLUME_DOWN, 0)
        assertNull(r)
    }

    @Test
    fun repeatsIgnored() {
        val d = HardwareShortcutDetector()
        assertNull(d.onKeyDown(0, HardwareShortcutDetector.KEYCODE_VOLUME_DOWN, 5))
    }
}
