package com.wavely.data.repository

import com.wavely.data.mediastore.MediaStoreSource
import com.wavely.domain.model.Track
import com.wavely.domain.model.TrackIdentity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaRepositoryImplTest {

    private fun track(uri: String, path: String, size: Long, date: Long) = Track(
        identity = TrackIdentity(filePath = path, sizeBytes = size, dateAddedSec = date),
        uri = uri,
        title = "T",
        artist = "A",
        album = "Al",
        albumId = 1,
        artistId = 1,
        durationMs = 1000,
        trackNumber = 1,
        year = 2020,
        folderPath = "/m",
        dateAddedSec = date,
        albumArtUri = null,
    )

    /**
     * Risk T-1 regression: a MediaStore re-index reassigns `_ID` (so the content
     * URI changes), but the stable identity (path + size + date_added) is unchanged.
     * Persisted references must still resolve.
     */
    @Test
    fun `resolveTracks survives a changed MediaStore id`() = runTest {
        val source = mockk<MediaStoreSource>()
        val persistedIdentity = TrackIdentity(filePath = "/m/song.mp3", sizeBytes = 4096, dateAddedSec = 111)

        // After re-index the URI changed from id 10 to id 99; identity is the same.
        every { source.queryTracks() } returns listOf(
            track(uri = "content://media/external/audio/media/99", path = "/m/song.mp3", size = 4096, date = 111),
        )

        val repo = MediaRepositoryImpl(source, Dispatchers.Unconfined)

        val resolved = repo.resolveTracks(listOf(persistedIdentity))

        assertEquals(1, resolved.size)
        assertEquals("content://media/external/audio/media/99", resolved.first().uri)
    }

    @Test
    fun `resolveTracks drops entries that no longer exist`() = runTest {
        val source = mockk<MediaStoreSource>()
        every { source.queryTracks() } returns listOf(
            track(uri = "content://x/1", path = "/m/a.mp3", size = 1, date = 1),
        )

        val repo = MediaRepositoryImpl(source, Dispatchers.Unconfined)

        val missing = TrackIdentity(filePath = "/m/deleted.mp3", sizeBytes = 9, dateAddedSec = 9)
        val resolved = repo.resolveTracks(listOf(missing))

        assertTrue(resolved.isEmpty())
    }
}
