package com.wavely.player.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavely.domain.model.Track
import com.wavely.domain.repository.PlaybackController
import com.wavely.domain.repository.PlaylistRepository
import com.wavely.player.navigation.Destination
import com.wavely.player.navigation.DetailType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val title: String = "",
    val tracks: List<Track> = emptyList(),
    val loading: Boolean = true,
    val isPlaylist: Boolean = false,
    val isFavorites: Boolean = false,
)

/** Detail for a user PLAYLIST (editable) or the FAVORITES list (Wavely v2 — no album/artist/folder). */
@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository,
    private val controller: PlaybackController,
) : ViewModel() {

    private val type: DetailType =
        DetailType.valueOf(savedStateHandle.get<String>(Destination.Detail.ARG_TYPE) ?: DetailType.PLAYLIST.name)
    private val id: String = savedStateHandle.get<String>(Destination.Detail.ARG_ID).orEmpty()
    private val playlistId: Long? = id.toLongOrNull()?.takeIf { type == DetailType.PLAYLIST }

    private val _state = MutableStateFlow(
        DetailUiState(isPlaylist = type == DetailType.PLAYLIST, isFavorites = type == DetailType.FAVORITES),
    )
    val state = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val tracks = when (type) {
                DetailType.PLAYLIST -> playlistRepository.playlistTracks(playlistId ?: -1)
                DetailType.FAVORITES -> playlistRepository.favoriteTracks()
            }
            _state.value = DetailUiState(
                title = titleFor(),
                tracks = tracks,
                loading = false,
                isPlaylist = type == DetailType.PLAYLIST,
                isFavorites = type == DetailType.FAVORITES,
            )
        }
    }

    private suspend fun titleFor(): String = when (type) {
        DetailType.PLAYLIST ->
            playlistRepository.observePlaylists().first().firstOrNull { it.id == playlistId }?.name ?: "Playlist"
        DetailType.FAVORITES -> "Favorites"
    }

    fun play(index: Int) = controller.playQueue(_state.value.tracks, index)

    fun shufflePlay() {
        val tracks = _state.value.tracks
        if (tracks.isEmpty()) return
        controller.setShuffle(true)
        controller.playQueue(tracks, 0)
    }

    fun playNext(track: Track) = controller.playNext(listOf(track))
    fun addToQueue(track: Track) = controller.addToQueue(listOf(track))

    // ----- Playlist editing (only when type == PLAYLIST) -----

    /** Move within the playlist; updates the screen immediately and persists. */
    fun moveTrack(from: Int, to: Int) {
        val pid = playlistId ?: return
        _state.update { s ->
            val list = s.tracks.toMutableList()
            if (from in list.indices && to in list.indices) {
                list.add(to, list.removeAt(from))
            }
            s.copy(tracks = list)
        }
        viewModelScope.launch { playlistRepository.reorderPlaylist(pid, from, to) }
    }

    fun removeTrack(track: Track) {
        val pid = playlistId ?: return
        _state.update { it.copy(tracks = it.tracks.filterNot { t -> t.identity == track.identity }) }
        viewModelScope.launch { playlistRepository.removeFromPlaylist(pid, track.identity) }
    }

    /** Undo a [removeTrack]: re-add and move back to [index]. */
    fun restoreTrack(index: Int, track: Track) {
        val pid = playlistId ?: return
        viewModelScope.launch {
            playlistRepository.addToPlaylist(pid, listOf(track.identity))
            val tracks = playlistRepository.playlistTracks(pid)
            val currentLast = tracks.lastIndex
            if (currentLast >= 0 && index in 0..currentLast) {
                playlistRepository.reorderPlaylist(pid, currentLast, index)
            }
            load()
        }
    }

    fun rename(name: String) {
        val pid = playlistId ?: return
        viewModelScope.launch {
            playlistRepository.renamePlaylist(pid, name)
            _state.update { it.copy(title = name) }
        }
    }

    fun deletePlaylist(onDone: () -> Unit) {
        val pid = playlistId ?: return
        viewModelScope.launch {
            playlistRepository.deletePlaylist(pid)
            onDone()
        }
    }

    // ----- Favorites editing (only when type == FAVORITES) -----

    fun removeFavorite(track: Track) {
        if (type != DetailType.FAVORITES) return
        _state.update { it.copy(tracks = it.tracks.filterNot { t -> t.identity == track.identity }) }
        viewModelScope.launch { playlistRepository.setFavorite(track.identity, false) }
    }

    fun restoreFavorite(track: Track) {
        if (type != DetailType.FAVORITES) return
        viewModelScope.launch {
            playlistRepository.setFavorite(track.identity, true)
            load()
        }
    }
}
