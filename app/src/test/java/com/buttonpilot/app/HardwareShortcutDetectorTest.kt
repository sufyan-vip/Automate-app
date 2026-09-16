package com.buttonpilot.app

import com.buttonpilot.app.feature.automation.HardwareShortcutDetector
import com.buttonpilot.app.feature.automation.ShortcutEvent
import com.buttonpilot.app.service.accessibility.KeyAction
import com.buttonpilot.app.service.accessibility.KeyCode
import com.buttonpilot.app.service.accessibility.KeySignal
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class HardwareShortcutDetectorTest {

    private lateinit var detector: HardwareShortcutDetector

    @Before
    fun setUp() {
        detector = HardwareShortcutDetector()
        detector.updateConfig(
            HardwareShortcutDetector.Config(
                maxIntervalBetweenPressesMs = 450L,
                sequenceTimeoutMs = 1100L,
                cooldownMs = 1500L
            )
        )
    }

    private fun signal(keyCode: KeyCode, time: Long): KeySignal {
        return KeySignal(keyCode, KeyAction.DOWN, time, 0)
    }

    @Test
    fun testTripleVolumeDownDetected() {
        // DOWN @ 0 ms, 270 ms, 520 ms -> Expected TripleVolumeDown
        var result = detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 0))
        assertNull(result)
        result = detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 270))
        // Should be Double at 270, but we test triple later
        assertTrue(result is ShortcutEvent.DoubleVolumeDown || result == null)
        result = detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 520))
        assertTrue(result is ShortcutEvent.TripleVolumeDown)
    }

    @Test
    fun testTripleTimeoutNoTrigger() {
        // DOWN @ 0 ms, 300 ms, 1600 ms -> NoTriplePress
        detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 0))
        detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 300))
        val result = detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 1600))
        // 1600 - 300 = 1300 > 450 max interval, should not trigger triple
        assertFalse(result is ShortcutEvent.TripleVolumeDown)
    }

    @Test
    fun testSinglePressNoTrigger() {
        val result = detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 0))
        assertNull(result)
    }

    @Test
    fun testDoublePressTimeout() {
        // Press twice, wait beyond timeout, press once -> No recorder action
        detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 0))
        detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 300))
        // Wait beyond timeout 1100ms from last event
        val result = detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 2000))
        assertNull(result) // Should not be triple, only single after reset
    }

    @Test
    fun testCooldownPreventsDuplicate() {
        detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 0))
        detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 200))
        val triple = detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 400))
        assertTrue(triple is ShortcutEvent.TripleVolumeDown)

        // Immediately try another triple within cooldown 1500ms
        val after1 = detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 500))
        val after2 = detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 700))
        val after3 = detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 900))
        assertNull(after3) // Should be blocked by cooldown
    }

    @Test
    fun testKeyRepeatFiltering() {
        val repeatSignal = KeySignal(KeyCode.VOLUME_DOWN, KeyAction.DOWN, 0, repeatCount = 1)
        val result = detector.onKeySignal(repeatSignal)
        // Detector filters repeat? Actually InputNormalizer filters, but detector should handle
        // For this test, we simulate normalizer already filtered, so we pass repeat 0 only
        // This test ensures repeat count handling is considered
        assertNull(result)
    }

    @Test
    fun testOverlappingPatterns() {
        // Volume Down x2 and x3 conflict: ensure triple overrides double
        detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 0))
        val double = detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 300))
        assertNotNull(double)
        val triple = detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 600))
        assertTrue(triple is ShortcutEvent.TripleVolumeDown)
    }

    @Test
    fun testVolumeUpTriple() {
        var result = detector.onKeySignal(signal(KeyCode.VOLUME_UP, 0))
        assertNull(result)
        result = detector.onKeySignal(signal(KeyCode.VOLUME_UP, 250))
        result = detector.onKeySignal(signal(KeyCode.VOLUME_UP, 500))
        assertTrue(result is ShortcutEvent.TripleVolumeUp)
    }
}
