package com.wavely.domain.repository

import com.wavely.domain.model.PlaybackUiState
import com.wavely.domain.model.RepeatMode
import com.wavely.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Abstraction over the Media3 player (MediaController bound to MediaSessionService).
 * The UI layer drives playback exclusively through this interface — it never
 * touches Media3 types directly. See design.md §10.1 / §10.4.
 */
interface PlaybackController {

    /** Single source of truth for playback, including the pre-connection window (risk T-3). */
    val state: StateFlow<PlaybackUiState>

    /**
     * Transient, user-facing playback errors (e.g. a corrupt/unsupported/deleted file was
     * skipped). The UI shows these as a toast/snackbar. See design.md §11 Stage 6 edge cases.
     */
    val errorEvents: Flow<String>

    /** Replace the queue with [tracks] and start playback at [startIndex]. */
    fun playQueue(tracks: List<Track>, startIndex: Int)

    /**
     * Restore a persisted session into PAUSED (never auto-play) at [positionMs].
     * Used on cold start. See design.md §10.3.
     */
    fun restoreSession(
        tracks: List<Track>,
        index: Int,
        positionMs: Long,
        shuffle: Boolean,
        repeat: RepeatMode,
        speed: Float,
    )

    fun play()

    fun pause()

    fun togglePlayPause()

    /** Skip to next track. */
    fun next()

    /** §2.2 prev rule: <3s → previous track; ≥3s → restart current. */
    fun previous()

    fun seekTo(positionMs: Long)

    fun setShuffle(enabled: Boolean)

    fun setRepeatMode(mode: RepeatMode)

    fun setSpeed(speed: Float)

    // Queue editing (§10.2)
    fun addToQueue(tracks: List<Track>)

    fun playNext(tracks: List<Track>)

    fun removeFromQueue(index: Int)

    /** Insert [tracks] at [index] (used to undo a swipe-removal). */
    fun insertIntoQueue(index: Int, tracks: List<Track>)

    fun moveQueueItem(from: Int, to: Int)

    fun skipToQueueIndex(index: Int)

    // Sleep timer (§2.2 utility row)
    fun setSleepTimer(endMs: Long?)
}
