package com.wavely.player.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {

    @Test
    fun `formats minutes and seconds`() {
        assertEquals("0:00", formatDuration(0))
        assertEquals("0:05", formatDuration(5_000))
        assertEquals("3:58", formatDuration(238_000))
    }

    @Test
    fun `formats hours when over an hour`() {
        assertEquals("1:00:00", formatDuration(3_600_000))
        assertEquals("1:01:05", formatDuration(3_665_000))
    }

    @Test
    fun `zero or negative duration is safe`() {
        assertEquals("0:00", formatDuration(-1))
        assertEquals("0:00", formatDuration(0))
    }

    @Test
    fun `remaining is negative and clamps at zero`() {
        assertEquals("-1:00", formatRemaining(positionMs = 60_000, durationMs = 120_000))
        assertEquals("-0:00", formatRemaining(positionMs = 130_000, durationMs = 120_000))
    }

    @Test
    fun `secondary line joins non-blank parts with middot`() {
        assertEquals("Artist · Album", secondaryLine("Artist", "Album"))
        assertEquals("Artist", secondaryLine("Artist", ""))
        assertEquals("Artist", secondaryLine("Artist", null))
    }
}
