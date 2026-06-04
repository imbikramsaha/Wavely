package com.wavely.player.ui.player

import org.junit.Assert.assertEquals
import org.junit.Test

class NextSpeedTest {

    @Test
    fun `cycles through the speed steps`() {
        assertEquals(0.75f, nextSpeed(0.5f), 0.001f)
        assertEquals(1.0f, nextSpeed(0.75f), 0.001f)
        assertEquals(1.25f, nextSpeed(1.0f), 0.001f)
        assertEquals(2.0f, nextSpeed(1.5f), 0.001f)
    }

    @Test
    fun `wraps from the top back to the slowest`() {
        assertEquals(0.5f, nextSpeed(2.0f), 0.001f)
    }
}
