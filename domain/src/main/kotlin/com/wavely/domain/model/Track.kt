package com.wavely.domain.model

/**
 * A single audio track surfaced from MediaStore.
 *
 * [identity] is the stable key used everywhere app-owned data references a track
 * (playlists, queue, favorites, counts). It must NOT be derived from the raw
 * MediaStore `_ID`, which can change on re-index / remount. See design.md §10.3, risk T-1.
 */
data class Track(
    val identity: TrackIdentity,
    /** Raw MediaStore content URI string, re-resolved on load. */
    val uri: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val artistId: Long,
    val durationMs: Long,
    val trackNumber: Int,
    val year: Int,
    /** Absolute file path of the containing folder (for the Folders tab). */
    val folderPath: String,
    val dateAddedSec: Long,
    /** URI used by Coil to load embedded art; may be null. */
    val albumArtUri: String?,
)
