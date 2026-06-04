package com.wavely.player.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavely.domain.model.Playlist
import com.wavely.domain.model.Track
import com.wavely.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Backs the add-to-playlist dialog and playlist create/rename/delete actions. */
@HiltViewModel
class PlaylistPickerViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    val playlists = playlistRepository.observePlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addToPlaylist(playlistId: Long, tracks: List<Track>) {
        viewModelScope.launch {
            playlistRepository.addToPlaylist(playlistId, tracks.map { it.identity })
        }
    }

    fun createAndAdd(name: String, tracks: List<Track>) {
        viewModelScope.launch {
            val id = playlistRepository.createPlaylist(name)
            if (tracks.isNotEmpty()) playlistRepository.addToPlaylist(id, tracks.map { it.identity })
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch { playlistRepository.createPlaylist(name) }
    }

    fun rename(playlist: Playlist, name: String) {
        viewModelScope.launch { playlistRepository.renamePlaylist(playlist.id, name) }
    }

    fun delete(playlist: Playlist) {
        viewModelScope.launch { playlistRepository.deletePlaylist(playlist.id) }
    }
}
