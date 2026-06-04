package com.wavely.player.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavely.domain.model.Track
import com.wavely.domain.repository.MediaRepository
import com.wavely.domain.repository.PlaybackController
import com.wavely.domain.repository.SearchResults
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val controller: PlaybackController,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    val results = _query
        .debounce(300) // §11 Stage 3: search debounced
        .mapLatest { q -> if (q.isBlank()) EMPTY else mediaRepository.search(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EMPTY)

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun playTrack(track: Track) {
        val tracks = results.value.tracks
        val index = tracks.indexOfFirst { it.identity == track.identity }.coerceAtLeast(0)
        controller.playQueue(tracks.ifEmpty { listOf(track) }, index)
    }

    companion object {
        private val EMPTY = SearchResults(emptyList())
    }
}
