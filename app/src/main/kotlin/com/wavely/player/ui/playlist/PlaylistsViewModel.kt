package com.wavely.player.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavely.domain.model.Playlist
import com.wavely.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Backs the Playlists tab: user playlists + a pinned Favorites entry. */
@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    val playlists = playlistRepository.observePlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoritesCount = playlistRepository.observeFavorites()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun createPlaylist(name: String) = viewModelScope.launch { playlistRepository.createPlaylist(name) }
    fun renamePlaylist(playlist: Playlist, name: String) =
        viewModelScope.launch { playlistRepository.renamePlaylist(playlist.id, name) }
    fun deletePlaylist(playlist: Playlist) = viewModelScope.launch { playlistRepository.deletePlaylist(playlist.id) }
}
