package com.wavely.domain.usecase

import com.wavely.domain.model.SortDirection
import com.wavely.domain.model.SortField
import com.wavely.domain.model.SortSpec
import com.wavely.domain.model.Track
import com.wavely.domain.model.TrackIdentity
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackSortingTest {

    private fun track(
        title: String,
        artist: String = "A",
        album: String = "Al",
        durationMs: Long = 1000,
        dateAddedSec: Long = 0,
        trackNumber: Int = 0,
    ) = Track(
        identity = TrackIdentity(filePath = "/m/$title.mp3", sizeBytes = 1, dateAddedSec = dateAddedSec),
        uri = "content://$title",
        title = title,
        artist = artist,
        album = album,
        albumId = 1,
        artistId = 1,
        durationMs = durationMs,
        trackNumber = trackNumber,
        year = 2020,
        folderPath = "/m",
        dateAddedSec = dateAddedSec,
        albumArtUri = null,
    )

    @Test
    fun `title ascending is case-insensitive`() {
        val sorted = TrackSorting.sortTracks(
            listOf(track("banana"), track("Apple"), track("cherry")),
            SortSpec(SortField.TITLE, SortDirection.ASC),
        )
        assertEquals(listOf("Apple", "banana", "cherry"), sorted.map { it.title })
    }

    @Test
    fun `title descending reverses`() {
        val sorted = TrackSorting.sortTracks(
            listOf(track("a"), track("b"), track("c")),
            SortSpec(SortField.TITLE, SortDirection.DESC),
        )
        assertEquals(listOf("c", "b", "a"), sorted.map { it.title })
    }

    @Test
    fun `duration ascending orders by length`() {
        val sorted = TrackSorting.sortTracks(
            listOf(track("x", durationMs = 300), track("y", durationMs = 100), track("z", durationMs = 200)),
            SortSpec(SortField.DURATION, SortDirection.ASC),
        )
        assertEquals(listOf("y", "z", "x"), sorted.map { it.title })
    }

    @Test
    fun `album sort breaks ties by track number`() {
        val sorted = TrackSorting.sortTracks(
            listOf(
                track("t3", album = "One", trackNumber = 3),
                track("t1", album = "One", trackNumber = 1),
                track("t2", album = "One", trackNumber = 2),
            ),
            SortSpec(SortField.ALBUM, SortDirection.ASC),
        )
        assertEquals(listOf("t1", "t2", "t3"), sorted.map { it.title })
    }
}
