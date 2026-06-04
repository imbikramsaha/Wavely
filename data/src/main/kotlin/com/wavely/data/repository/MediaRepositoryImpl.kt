package com.wavely.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.wavely.data.mediastore.ListPagingSource
import com.wavely.data.mediastore.MediaStoreSource
import com.wavely.domain.model.SortSpec
import com.wavely.domain.model.Track
import com.wavely.domain.model.TrackIdentity
import com.wavely.domain.repository.MediaRepository
import com.wavely.domain.repository.SearchResults
import com.wavely.domain.usecase.TrackSorting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val source: MediaStoreSource,
    private val io: CoroutineDispatcher,
) : MediaRepository {

    // In-memory snapshot of the whole library, refreshed on rescan.
    private val tracksSnapshot = MutableStateFlow<List<Track>>(emptyList())

    /** Re-resolution index: identity key → Track, rebuilt on every scan (risk T-1). */
    private val byIdentity = MutableStateFlow<Map<String, Track>>(emptyMap())

    private val libraryChanges = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)

    // Starts true so the song list shows a spinner (not "No music") until the first scan lands.
    // Only ever flips to false — later content-change rescans must not re-show a spinner over an
    // existing list.
    private val loading = MutableStateFlow(true)

    private val pagingConfig = PagingConfig(pageSize = 60, prefetchDistance = 30, enablePlaceholders = false)

    private suspend fun ensureLoaded() {
        if (tracksSnapshot.value.isEmpty()) rescan()
    }

    override fun isLoading(): Flow<Boolean> = loading.asStateFlow()

    override suspend fun rescan() = withContext(io) {
        val tracks = source.queryTracks()
        tracksSnapshot.value = tracks
        byIdentity.value = tracks.associateBy { it.identity.asKey() }
        loading.value = false
        libraryChanges.tryEmit(Unit)
        Unit
    }

    override fun tracks(sort: SortSpec): Flow<PagingData<Track>> =
        tracksSnapshot.onStart { ensureLoaded() }.flatMapLatest { snapshot ->
            val sorted = TrackSorting.sortTracks(snapshot, sort)
            Pager(pagingConfig) { ListPagingSource(sorted) }.flow
        }

    override suspend fun allTracks(sort: SortSpec): List<Track> = withContext(io) {
        ensureLoaded()
        TrackSorting.sortTracks(tracksSnapshot.value, sort)
    }

    override suspend fun resolveTracks(identities: List<TrackIdentity>): List<Track> = withContext(io) {
        ensureLoaded()
        val index = byIdentity.value
        // Drop entries that no longer resolve (file removed / re-index) — risk T-1.
        identities.mapNotNull { index[it.asKey()] }
    }

    override suspend fun search(query: String): SearchResults = withContext(io) {
        ensureLoaded()
        val q = query.trim()
        if (q.isBlank()) return@withContext SearchResults(emptyList())
        fun String.match() = contains(q, ignoreCase = true)
        SearchResults(
            tracks = tracksSnapshot.value.filter { it.title.match() || it.artist.match() || it.album.match() }.take(80),
        )
    }

    override fun observeLibraryChanges(): Flow<Unit> = libraryChanges
}
