package com.wavely.player.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.wavely.domain.model.SortSpec
import com.wavely.domain.model.SortableList
import com.wavely.domain.model.Track
import com.wavely.domain.repository.MediaRepository
import com.wavely.domain.repository.PlaybackController
import com.wavely.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Backs the flat Songs list (Wavely v2 — no album/artist/folder browse). */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val settings: SettingsRepository,
    private val controller: PlaybackController,
) : ViewModel() {

    val tracksSort = settings.sortSpec(SortableList.TRACKS)
        .stateIn(viewModelScope, SharingStarted.Eagerly, SortSpec())

    val tracks = tracksSort.flatMapLatest { mediaRepository.tracks(it) }.cachedIn(viewModelScope)

    /** True while the first MediaStore scan is in flight — drives the spinner vs "no music". */
    val loading = mediaRepository.isLoading()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    /** Play the whole song list, starting at the tapped track (§9 tap behavior). */
    fun onTrackSelected(track: Track) {
        viewModelScope.launch {
            val all = mediaRepository.allTracks(tracksSort.value)
            val index = all.indexOfFirst { it.identity == track.identity }.coerceAtLeast(0)
            controller.playQueue(all, index)
        }
    }

    /** Shuffle the whole song list. */
    fun shuffleAll() {
        viewModelScope.launch {
            val all = mediaRepository.allTracks(tracksSort.value)
            if (all.isEmpty()) return@launch
            controller.setShuffle(true)
            controller.playQueue(all, 0)
        }
    }

    fun playNext(tracks: List<Track>) = controller.playNext(tracks)

    fun addToQueue(tracks: List<Track>) = controller.addToQueue(tracks)

    fun setSort(spec: SortSpec) {
        viewModelScope.launch { settings.setSortSpec(SortableList.TRACKS, spec) }
    }

    fun rescan() {
        viewModelScope.launch { mediaRepository.rescan() }
    }
}
