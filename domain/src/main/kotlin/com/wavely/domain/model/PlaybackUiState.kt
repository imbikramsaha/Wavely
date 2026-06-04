package com.wavely.domain.model

/**
 * Single source of truth for playback, surfaced from the Media3 Player.
 * See design.md §10.1. Mirrors the spec's `PlaybackUiState` (uses [Track]
 * directly rather than a separate `TrackUi` type).
 */
data class PlaybackUiState(
    val current: Track? = null, // null = nothing loaded
    val queue: List<Track> = emptyList(),
    val queueIndex: Int = -1,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeat: RepeatMode = RepeatMode.OFF,
    val speed: Float = 1.0f,
    val sleepTimerEndMs: Long? = null,
    /** True until the MediaController has bound to the service (risk T-3). */
    val isConnecting: Boolean = true,
) {
    val hasTrack: Boolean get() = current != null

    companion object {
        val Empty = PlaybackUiState()
    }
}
