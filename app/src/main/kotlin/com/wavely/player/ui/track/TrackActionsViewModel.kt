package com.wavely.player.ui.track

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavely.domain.model.Track
import com.wavely.domain.repository.PlaybackController
import com.wavely.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Shared per-row actions (play next, add to queue, favorite) reused across list screens. */
@HiltViewModel
class TrackActionsViewModel @Inject constructor(
    private val controller: PlaybackController,
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    val favorites = playlistRepository.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun playNext(track: Track) = controller.playNext(listOf(track))
    fun addToQueue(track: Track) = controller.addToQueue(listOf(track))

    fun isFavorite(track: Track): Boolean = favorites.value.contains(track.identity.asKey())

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            playlistRepository.setFavorite(track.identity, !isFavorite(track))
        }
    }
}
