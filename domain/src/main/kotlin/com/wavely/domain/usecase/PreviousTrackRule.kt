package com.wavely.domain.usecase

/**
 * The "previous" button rule (design.md §2.2): if more than [thresholdMs] of the
 * current track has elapsed, restart it; otherwise skip to the previous track.
 * Pure function so it can be unit-tested without a player (Stage 1 exit).
 */
object PreviousTrackRule {

    const val DEFAULT_THRESHOLD_MS = 3_000L

    sealed interface Action {
        data object RestartCurrent : Action
        data object PreviousTrack : Action
    }

    fun decide(positionMs: Long, hasPrevious: Boolean, thresholdMs: Long = DEFAULT_THRESHOLD_MS): Action =
        if (positionMs >= thresholdMs || !hasPrevious) Action.RestartCurrent else Action.PreviousTrack
}
