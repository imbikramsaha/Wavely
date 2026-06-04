package com.wavely.player.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavely.data.playback.PlaybackConnection
import com.wavely.domain.model.PlaybackUiState
import com.wavely.domain.model.RepeatMode
import com.wavely.domain.model.Track
import com.wavely.domain.repository.MediaRepository
import com.wavely.domain.repository.PersistedSession
import com.wavely.domain.repository.PlaybackController
import com.wavely.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val controller: PlaybackController,
    private val connection: PlaybackConnection,
    private val playlistRepository: PlaylistRepository,
    private val mediaRepository: MediaRepository,
) : ViewModel() {

    val state = controller.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlaybackUiState.Empty,
    )

    val favorites = playlistRepository.observeFavorites().stateIn(
        scope = viewModelScope,
        // Eagerly so the Now Playing favorite icon reflects the correct state immediately
        // (flaw #2: it briefly showed a stale/wrong state under WhileSubscribed).
        started = SharingStarted.Eagerly,
        initialValue = emptySet(),
    )

    /** Transient playback errors (skipped corrupt/missing tracks) for the UI to toast. */
    val errorEvents = controller.errorEvents

    init {
        restoreSessionOnce()
        persistSession()
    }

    // ----- Playback commands -----
    fun playTracks(tracks: List<Track>, startIndex: Int) = controller.playQueue(tracks, startIndex)
    fun play() = controller.play()
    fun pause() = controller.pause()
    fun togglePlayPause() = controller.togglePlayPause()
    fun next() = controller.next()
    fun previous() = controller.previous()
    fun seekTo(positionMs: Long) = controller.seekTo(positionMs)
    fun toggleShuffle() = controller.setShuffle(!state.value.shuffle)
    fun cycleRepeat() = controller.setRepeatMode(state.value.repeat.next())
    fun setSpeed(speed: Float) = controller.setSpeed(speed)
    fun setSleepTimer(endMs: Long?) = controller.setSleepTimer(endMs)

    // Queue editing
    fun skipToQueueIndex(index: Int) = controller.skipToQueueIndex(index)
    fun removeFromQueue(index: Int) = controller.removeFromQueue(index)

    /**
     * Undo a queue removal. Rather than trusting the index captured when the row was swiped
     * (which goes stale if the queue is edited again before Undo is tapped), re-anchor against
     * the LIVE queue: drop the track back in right after [anchorKey] (the item that preceded it),
     * falling back to [fallbackIndex] (clamped) if the anchor is gone or the track was first.
     */
    fun reinsertIntoQueue(track: Track, anchorKey: String?, fallbackIndex: Int) {
        val queue = state.value.queue
        val at = when {
            anchorKey == null -> 0
            else -> queue.indexOfFirst { it.identity.asKey() == anchorKey }
                .let { if (it >= 0) it + 1 else fallbackIndex }
        }
        controller.insertIntoQueue(at.coerceIn(0, queue.size), listOf(track))
    }

    fun moveQueueItem(from: Int, to: Int) = controller.moveQueueItem(from, to)
    fun playNext(tracks: List<Track>) = controller.playNext(tracks)
    fun addToQueue(tracks: List<Track>) = controller.addToQueue(tracks)

    /** Gate the position ticker to UI visibility for battery (§10.1). */
    fun onUiActive(active: Boolean) = connection.setUiActive(active)

    /**
     * Persist the session immediately (not debounced). Called on backgrounding so position
     * survives process death / low-memory kill (§10.3, Stage 6 restore edge case).
     */
    fun persistNow() {
        val s = state.value
        if (!s.hasTrack) return
        viewModelScope.launch {
            playlistRepository.saveSession(
                PersistedSession(
                    queueIdentities = s.queue.map { it.identity },
                    index = s.queueIndex.coerceAtLeast(0),
                    positionMs = s.positionMs,
                    shuffle = s.shuffle,
                    repeatOrdinal = s.repeat.ordinal,
                    speed = s.speed,
                ),
            )
        }
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            val isFav = favorites.value.contains(track.identity.asKey())
            playlistRepository.setFavorite(track.identity, !isFav)
        }
    }

    fun isFavorite(track: Track?): Boolean =
        track != null && favorites.value.contains(track.identity.asKey())

    private fun restoreSessionOnce() {
        viewModelScope.launch {
            controller.state.first { !it.isConnecting }
            if (controller.state.value.hasTrack) return@launch
            val session = playlistRepository.loadSession() ?: return@launch
            val tracks = mediaRepository.resolveTracks(session.queueIdentities)
            if (tracks.isEmpty()) return@launch
            controller.restoreSession(
                tracks = tracks,
                index = session.index.coerceIn(0, tracks.lastIndex),
                positionMs = session.positionMs,
                shuffle = session.shuffle,
                repeat = RepeatMode.entries.getOrElse(session.repeatOrdinal) { RepeatMode.OFF },
                speed = session.speed,
            )
        }
    }

    @OptIn(FlowPreview::class)
    private fun persistSession() {
        viewModelScope.launch {
            controller.state
                .filter { it.hasTrack }
                .map { s ->
                    PersistedSession(
                        queueIdentities = s.queue.map { it.identity },
                        index = s.queueIndex.coerceAtLeast(0),
                        positionMs = s.positionMs,
                        shuffle = s.shuffle,
                        repeatOrdinal = s.repeat.ordinal,
                        speed = s.speed,
                    )
                }
                .distinctUntilChanged()
                .debounce(2_000) // §10.3 debounced persistence
                .collect { playlistRepository.saveSession(it) }
        }
    }
}
