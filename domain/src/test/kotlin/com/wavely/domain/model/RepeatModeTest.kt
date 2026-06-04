package com.wavely.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RepeatModeTest {

    @Test
    fun `cycles off to all to one to off`() {
        assertEquals(RepeatMode.ALL, RepeatMode.OFF.next())
        assertEquals(RepeatMode.ONE, RepeatMode.ALL.next())
        assertEquals(RepeatMode.OFF, RepeatMode.ONE.next())
    }
}
