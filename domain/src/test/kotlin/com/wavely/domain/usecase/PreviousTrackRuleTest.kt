package com.wavely.domain.usecase

import com.wavely.domain.usecase.PreviousTrackRule.Action
import org.junit.Assert.assertEquals
import org.junit.Test

class PreviousTrackRuleTest {

    @Test
    fun `before threshold with previous goes to previous track`() {
        assertEquals(Action.PreviousTrack, PreviousTrackRule.decide(positionMs = 1_500, hasPrevious = true))
    }

    @Test
    fun `at threshold restarts current`() {
        assertEquals(Action.RestartCurrent, PreviousTrackRule.decide(positionMs = 3_000, hasPrevious = true))
    }

    @Test
    fun `after threshold restarts current`() {
        assertEquals(Action.RestartCurrent, PreviousTrackRule.decide(positionMs = 9_000, hasPrevious = true))
    }

    @Test
    fun `no previous track always restarts even early`() {
        assertEquals(Action.RestartCurrent, PreviousTrackRule.decide(positionMs = 200, hasPrevious = false))
    }
}
