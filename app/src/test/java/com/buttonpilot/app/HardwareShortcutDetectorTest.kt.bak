package com.buttonpilot.app

import com.buttonpilot.app.feature.automation.HardwareShortcutDetector
import com.buttonpilot.app.feature.automation.ShortcutEvent
import com.buttonpilot.app.service.accessibility.KeyAction
import com.buttonpilot.app.service.accessibility.KeyCode
import com.buttonpilot.app.service.accessibility.KeySignal
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Pure JVM unit tests for the triple-press Volume Down state machine.
 *
 * These tests correspond to Mandatory Acceptance Tests A–D in the master spec:
 *  - Test A: triple press detected
 *  - Test C: single press no-op
 *  - Test D: timeout resets sequence
 *  - Cooldown prevents duplicate triple-fires
 *  - Double-press is flushed after interval elapses
 */
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

    private fun down(keyCode: KeyCode, time: Long, repeatCount: Int = 0): KeySignal =
        KeySignal(keyCode, KeyAction.DOWN, time, repeatCount)

    @Test
    fun `Test A - triple Volume Down detected within interval`() {
        assertNull(detector.onKeySignal(down(KeyCode.VOLUME_DOWN, 0)))
        assertNull(detector.onKeySignal(down(KeyCode.VOLUME_DOWN, 270))) // double held for conflict resolution
        val result = detector.onKeySignal(down(KeyCode.VOLUME_DOWN, 520))
        assertTrue("Expected TripleVolumeDown, got $result", result is ShortcutEvent.TripleVolumeDown)
    }

    @Test
    fun `Test C - single Volume Down does not trigger anything`() {
        assertNull(detector.onKeySignal(down(KeyCode.VOLUME_DOWN, 0)))
    }

    @Test
    fun `Test D - press twice, wait past timeout, press once produces no triple`() {
        detector.onKeySignal(down(KeyCode.VOLUME_DOWN, 0))
        detector.onKeySignal(down(KeyCode.VOLUME_DOWN, 300))
        val third = detector.onKeySignal(down(KeyCode.VOLUME_DOWN, 2000))
        assertFalse("Triple must not fire after timeout, got $third", third is ShortcutEvent.TripleVolumeDown)
        assertNull(detector.flushPending(2000))
    }

    @Test
    fun triple timeout at interval boundary does not fire() {
        // 0, 300, 1600: gap between 300 and 1600 is 1300 > 450 maxInterval
        detector.onKeySignal(down(KeyCode.VOLUME_DOWN, 0))
        detector.onKeySignal(down(KeyCode.VOLUME_DOWN, 300))
        val result = detector.onKeySignal(down(KeyCode.VOLUME_DOWN, 1600))
        assertFalse(result is ShortcutEvent.TripleVolumeDown)
    }

    @Test
    fun double press is flushed after interval elapses with no third press() {
        detector.onKeySignal(down(KeyCode.VOLUME_DOWN, 0))
        assertNull(detector.onKeySignal(down(KeyCode.VOLUME_DOWN, 300)))
        assertNull("Must not flush before interval elapses", detector.flushPending(700))
        val flushed = detector.flushPending(800)
        assertTrue("Expected DoubleVolumeDown after waiting, got $flushed", flushed is ShortcutEvent.DoubleVolumeDown)
    }

    @Test
    fun cooldown prevents immediate duplicate triple() {
        detector.onKeySignal(down(KeyCode.VOLUME_DOWN, 0))
        detector.onKeySignal(down(KeyCode.VOLUME_DOWN, 200))
        val triple = detector.onKeySignal(down(KeyCode.VOLUME_DOWN, 400))
        assertTrue(triple is ShortcutEvent.TripleVolumeDown)

        detector.onKeySignal(down(KeyCode.VOLUME_DOWN, 500))
        detector.onKeySignal(down(KeyCode.VOLUME_DOWN, 700))
        val third = detector.onKeySignal(down(KeyCode.VOLUME_DOWN, 900))
        assertNull("Cooldown should block duplicate triple, got $third", third)
    }

    @Test
    fun triple Volume Up works symmetrically() {
        assertNull(detector.onKeySignal(down(KeyCode.VOLUME_UP, 0)))
        assertNull(detector.onKeySignal(down(KeyCode.VOLUME_UP, 250)))
        val triple = detector.onKeySignal(down(KeyCode.VOLUME_UP, 500))
        assertTrue(triple is ShortcutEvent.TripleVolumeUp)
    }

    @Test
    fun reset clears state() {
        detector.onKeySignal(down(KeyCode.VOLUME_DOWN, 0))
        detector.reset()
        val state = detector.getCurrentState()
        assertEquals(0, state.downPressCount)
        assertEquals(0, state.upPressCount)
    }
}
