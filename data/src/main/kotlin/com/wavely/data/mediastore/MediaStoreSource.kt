package com.wavely.data.mediastore

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.wavely.domain.model.Track
import com.wavely.domain.model.TrackIdentity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Raw MediaStore.Audio access. Returns domain models. Builds a stable
 * [TrackIdentity] per track (path + size + date_added) — never relies on `_ID`
 * for app-owned references (design.md §6.1, §10.3, risk T-1).
 */
@Singleton
class MediaStoreSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val resolver: ContentResolver get() = context.contentResolver

    private val audioCollection: Uri =
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    private val albumArtBase: Uri = Uri.parse("content://media/external/audio/albumart")

    fun queryTracks(): List<Track> {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        val result = ArrayList<Track>()
        try {
        resolver.query(audioCollection, projection, selection, null, null)?.use { c: Cursor ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val artistIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val yearCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val sizeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val path = c.getString(dataCol) ?: continue
                val size = c.getLong(sizeCol)
                val dateAdded = c.getLong(dateCol)
                val albumId = c.getLong(albumIdCol)
                // TRACK is encoded as disc*1000 + track; normalize to within-disc number.
                val rawTrack = c.getInt(trackCol)
                val trackNo = if (rawTrack > 1000) rawTrack % 1000 else rawTrack

                result += Track(
                    identity = TrackIdentity(filePath = path, sizeBytes = size, dateAddedSec = dateAdded),
                    uri = ContentUris.withAppendedId(audioCollection, id).toString(),
                    title = c.getString(titleCol).orUnknown(UNKNOWN_TITLE),
                    artist = c.getString(artistCol).orUnknown(UNKNOWN_ARTIST),
                    album = c.getString(albumCol).orUnknown(UNKNOWN_ALBUM),
                    albumId = albumId,
                    artistId = c.getLong(artistIdCol),
                    durationMs = c.getLong(durationCol),
                    trackNumber = trackNo,
                    year = c.getInt(yearCol),
                    folderPath = File(path).parent ?: "",
                    dateAddedSec = dateAdded,
                    albumArtUri = ContentUris.withAppendedId(albumArtBase, albumId).toString(),
                )
            }
        }
        } catch (e: SecurityException) {
            // Audio permission was revoked while running — treat as an empty library.
            return emptyList()
        } catch (e: Exception) {
            // Provider failure / corrupt cursor — return whatever was read so far.
        }
        // De-dup by identity: MediaStore can occasionally surface two rows with the same
        // (path|size|dateAdded) tuple. Identical identities would collide as Compose list keys
        // and crash the song list ("key was already used"), so collapse them here at the source.
        return result.distinctBy { it.identity.asKey() }
    }

    private fun String?.orUnknown(fallback: String): String =
        if (this.isNullOrBlank() || this == "<unknown>") fallback else this

    companion object {
        const val UNKNOWN_TITLE = "Unknown title"
        const val UNKNOWN_ARTIST = "Unknown artist"
        const val UNKNOWN_ALBUM = "Unknown album"
    }
}
