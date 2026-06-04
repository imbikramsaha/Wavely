package com.wavely.domain.model

/**
 * Stable, re-resolvable identity for a track. Composite of properties that
 * survive a MediaStore re-index or SD-card remount (which reassign `_ID`).
 *
 * Persisted in Room as the foreign key for playlists / queue / favorites / counts,
 * and re-resolved against MediaStore on load. See design.md §10.3 and risk T-1.
 */
data class TrackIdentity(
    val filePath: String,
    val sizeBytes: Long,
    val dateAddedSec: Long,
) {
    /** Single-string form for storage / lookup keys. */
    fun asKey(): String = "$filePath|$sizeBytes|$dateAddedSec"

    companion object {
        fun fromKey(key: String): TrackIdentity? {
            val parts = key.split("|")
            if (parts.size != 3) return null
            val size = parts[1].toLongOrNull() ?: return null
            val date = parts[2].toLongOrNull() ?: return null
            return TrackIdentity(filePath = parts[0], sizeBytes = size, dateAddedSec = date)
        }
    }
}
