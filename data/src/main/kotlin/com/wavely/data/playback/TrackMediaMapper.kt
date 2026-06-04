package com.wavely.data.playback

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.wavely.domain.model.Track
import com.wavely.domain.model.TrackIdentity

/**
 * Converts between the domain [Track] and a Media3 [MediaItem]. The full track
 * payload is packed into the metadata extras so it can be reconstructed from the
 * controller's current item without a separate lookup. The [MediaItem.mediaId]
 * is the stable identity key (risk T-1), not the MediaStore `_ID`.
 */
object TrackMediaMapper {

    private const val KEY_URI = "listn.uri"
    private const val KEY_ALBUM_ID = "listn.albumId"
    private const val KEY_ARTIST_ID = "listn.artistId"
    private const val KEY_TRACK_NO = "listn.trackNo"
    private const val KEY_YEAR = "listn.year"
    private const val KEY_FOLDER = "listn.folder"
    private const val KEY_SIZE = "listn.size"
    private const val KEY_DATE_ADDED = "listn.dateAdded"
    private const val KEY_DURATION = "listn.duration"
    private const val KEY_FILE_PATH = "listn.path"
    private const val KEY_ART = "listn.art"

    fun toMediaItem(track: Track): MediaItem {
        val extras = Bundle().apply {
            putString(KEY_URI, track.uri)
            putLong(KEY_ALBUM_ID, track.albumId)
            putLong(KEY_ARTIST_ID, track.artistId)
            putInt(KEY_TRACK_NO, track.trackNumber)
            putInt(KEY_YEAR, track.year)
            putString(KEY_FOLDER, track.folderPath)
            putLong(KEY_SIZE, track.identity.sizeBytes)
            putLong(KEY_DATE_ADDED, track.identity.dateAddedSec)
            putLong(KEY_DURATION, track.durationMs)
            putString(KEY_FILE_PATH, track.identity.filePath)
            track.albumArtUri?.let { putString(KEY_ART, it) }
        }
        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .setArtworkUri(track.albumArtUri?.let(Uri::parse))
            .setExtras(extras)
            .build()
        return MediaItem.Builder()
            .setMediaId(track.identity.asKey())
            .setUri(track.uri)
            .setMediaMetadata(metadata)
            .build()
    }

    fun fromMediaItem(item: MediaItem?): Track? {
        item ?: return null
        val md = item.mediaMetadata
        val extras = md.extras ?: return null
        val path = extras.getString(KEY_FILE_PATH) ?: return null
        return Track(
            identity = TrackIdentity(
                filePath = path,
                sizeBytes = extras.getLong(KEY_SIZE),
                dateAddedSec = extras.getLong(KEY_DATE_ADDED),
            ),
            uri = extras.getString(KEY_URI) ?: item.localConfiguration?.uri?.toString() ?: "",
            title = md.title?.toString() ?: "",
            artist = md.artist?.toString() ?: "",
            album = md.albumTitle?.toString() ?: "",
            albumId = extras.getLong(KEY_ALBUM_ID),
            artistId = extras.getLong(KEY_ARTIST_ID),
            durationMs = extras.getLong(KEY_DURATION),
            trackNumber = extras.getInt(KEY_TRACK_NO),
            year = extras.getInt(KEY_YEAR),
            folderPath = extras.getString(KEY_FOLDER) ?: "",
            dateAddedSec = extras.getLong(KEY_DATE_ADDED),
            albumArtUri = extras.getString(KEY_ART),
        )
    }
}
