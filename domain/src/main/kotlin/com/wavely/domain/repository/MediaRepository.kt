package com.wavely.domain.repository

import androidx.paging.PagingData
import com.wavely.domain.model.SortSpec
import com.wavely.domain.model.Track
import com.wavely.domain.model.TrackIdentity
import kotlinx.coroutines.flow.Flow

/**
 * Read access to the on-device audio library, backed by MediaStore.
 * The library is a single flat song list (Wavely v2 — no album/artist/folder browse).
 * Lists are paged (Paging 3) for large libraries — see design.md §6.1, risk T-7.
 */
interface MediaRepository {

    fun tracks(sort: SortSpec): Flow<PagingData<Track>>

    /**
     * True until the first MediaStore scan completes. Lets the UI tell "still scanning"
     * (show a spinner) apart from "scanned and genuinely empty" (show the empty state).
     */
    fun isLoading(): Flow<Boolean>

    /** Full sorted track list — used to build a queue when playing from the song list. */
    suspend fun allTracks(sort: SortSpec): List<Track>

    /** Re-resolve persisted identities against the current MediaStore (queue/playlist restore). */
    suspend fun resolveTracks(identities: List<TrackIdentity>): List<Track>

    /** Search across the song list. */
    suspend fun search(query: String): SearchResults

    /** Trigger a MediaStore rescan; emits when complete. */
    suspend fun rescan()

    /** Emits whenever the underlying MediaStore content changes. */
    fun observeLibraryChanges(): Flow<Unit>
}

data class SearchResults(
    val tracks: List<Track>,
) {
    val isEmpty: Boolean get() = tracks.isEmpty()
}
