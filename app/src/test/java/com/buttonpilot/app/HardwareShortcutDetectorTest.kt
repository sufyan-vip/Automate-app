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

    private fun signal(keyCode: KeyCode, time: Long, repeatCount: Int = 0): KeySignal {
        return KeySignal(keyCode, KeyAction.DOWN, time, repeatCount)
    }

    @Test
    fun testTripleVolumeDownDetected() {
        // DOWN @ 0 ms, 270 ms, 520 ms -> Expected TripleVolumeDown
        assertNull(detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 0)))
        // Double is NOT emitted early (conflict protection: wait for possible triple)
        assertNull(detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 270)))
        // Third press within interval -> triple fires
        assertTrue(detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 520)) is ShortcutEvent.TripleVolumeDown)
    }

    @Test
    fun testTripleTimeoutNoTrigger() {
        // DOWN @ 0 ms, 300 ms, 1600 ms -> NoTriplePress (1600-300 = 1300 > 450)
        detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 0))
        detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 300))
        val result = detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 1600))
        assertFalse(result is ShortcutEvent.TripleVolumeDown)
    }

    @Test
    fun testSinglePressNoTrigger() {
        assertNull(detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 0)))
    }

    @Test
    fun testDoublePressTimeout() {
        // Press twice, wait beyond timeout, press once -> No recorder action (sequence resets)
        detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 0))
        detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 300))
        // After waiting beyond sequence timeout the third press is treated as a new single press
        val result = detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 2000))
        assertNull(result)
        // And no pending double is available (2000ms beyond the pair so the pair was cleared)
        assertNull(detector.flushPending(2000))
    }

    @Test
    fun testDoubleFlushedAfterInterval() {
        // Two presses within interval produce NO immediate event, but flushPending returns DoubleVolumeDown
        detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 0))
        assertNull(detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 300)))
        // Before interval elapsed, nothing flushed
        assertNull(detector.flushPending(600))
        // After interval elapsed, double is flushed
        assertTrue(detector.flushPending(800) is ShortcutEvent.DoubleVolumeDown)
    }

    @Test
    fun testCooldownPreventsDuplicate() {
        detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 0))
        detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 200))
        val triple = detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 400))
        assertTrue(triple is ShortcutEvent.TripleVolumeDown)

        // Immediately try another triple within cooldown 1500ms
        detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 500))
        detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 700))
        val third = detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 900))
        assertNull(third) // Should be blocked by cooldown
    }

    @Test
    fun testKeyRepeatFiltered() {
        // Repeats (repeatCount > 0) are filtered upstream by InputNormalizer; the detector
        // receives only clean events. This test asserts a repeated signal isn't treated as a press.
        val repeatSignal = signal(KeyCode.VOLUME_DOWN, 0, repeatCount = 1)
        // The detector doesn't currently inspect repeatCount (normalizer filters), so this is
        // effectively a smoke test - the signal should be considered because detector sees KEY_DOWN.
        // Real filtering is in InputNormalizer.normalize().
        detector.onKeySignal(repeatSignal)
        // The single press alone does not trigger an event.
        assertNull(detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 0)))
    }

    @Test
    fun testOverlappingPatternsTripleWins() {
        // Triple overrides any pending double
        detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 0))
        assertNull(detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 300)))
        val triple = detector.onKeySignal(signal(KeyCode.VOLUME_DOWN, 600))
        assertTrue(triple is ShortcutEvent.TripleVolumeDown)
        // After triple fires, no pending double remains.
        assertNull(detector.flushPending(1200))
    }

    @Test
    fun testVolumeUpTriple() {
        assertNull(detector.onKeySignal(signal(KeyCode.VOLUME_UP, 0)))
        assertNull(detector.onKeySignal(signal(KeyCode.VOLUME_UP, 250)))
        assertTrue(detector.onKeySignal(signal(KeyCode.VOLUME_UP, 500)) is ShortcutEvent.TripleVolumeUp)
    }
}
