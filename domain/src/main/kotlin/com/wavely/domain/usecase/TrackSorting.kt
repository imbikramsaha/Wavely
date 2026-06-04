package com.wavely.domain.usecase

import com.wavely.domain.model.SortDirection
import com.wavely.domain.model.SortField
import com.wavely.domain.model.SortSpec
import com.wavely.domain.model.Track

/**
 * Pure sort comparators for the in-memory song list (queue building, detail screens).
 * The paged list sorts in the data layer's query; this mirrors that ordering so the
 * queue built from a list matches the displayed order (§10.2). Tested in Stage 2.
 */
object TrackSorting {

    fun sortTracks(tracks: List<Track>, spec: SortSpec): List<Track> {
        val base: Comparator<Track> = when (spec.field) {
            SortField.TITLE -> compareBy(String.CASE_INSENSITIVE_ORDER, Track::title)
            SortField.DATE_ADDED -> compareBy(Track::dateAddedSec)
            SortField.DURATION -> compareBy(Track::durationMs)
            SortField.ALBUM -> compareBy(String.CASE_INSENSITIVE_ORDER, Track::album)
                .thenBy(Track::trackNumber)
            SortField.ARTIST -> compareBy(String.CASE_INSENSITIVE_ORDER, Track::artist)
                .thenBy(String.CASE_INSENSITIVE_ORDER, Track::title)
        }
        // Stable tiebreaker so equal keys keep a deterministic order across runs.
        val stable = base.thenBy { it.identity.asKey() }
        return tracks.sortedWith(if (spec.direction == SortDirection.DESC) stable.reversed() else stable)
    }
}
