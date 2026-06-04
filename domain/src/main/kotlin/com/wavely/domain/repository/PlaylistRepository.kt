package com.wavely.domain.repository

import com.wavely.domain.model.Playlist
import com.wavely.domain.model.Track
import com.wavely.domain.model.TrackIdentity
import kotlinx.coroutines.flow.Flow

/**
 * App-owned, durable data persisted in Room and keyed by stable [TrackIdentity]
 * (never the raw MediaStore `_ID`). Covers playlists, favorites, play history,
 * play counts, and the persisted/restored session. See design.md §6.1, §10.3.
 */
interface PlaylistRepository {

    // ----- Playlists (CRUD) -----
    fun observePlaylists(): Flow<List<Playlist>>

    suspend fun createPlaylist(name: String): Long

    suspend fun renamePlaylist(playlistId: Long, name: String)

    suspend fun deletePlaylist(playlistId: Long)

    suspend fun addToPlaylist(playlistId: Long, identities: List<TrackIdentity>)

    suspend fun removeFromPlaylist(playlistId: Long, identity: TrackIdentity)

    suspend fun reorderPlaylist(playlistId: Long, from: Int, to: Int)

    /** Resolved tracks of a playlist, in order; unresolved entries dropped (marked missing). */
    suspend fun playlistTracks(playlistId: Long): List<Track>

    // ----- Favorites (quick playlist) -----
    fun observeFavorites(): Flow<Set<String>> // set of TrackIdentity keys

    suspend fun setFavorite(identity: TrackIdentity, favorite: Boolean)

    suspend fun favoriteTracks(): List<Track>

    // ----- History & counts -----
    suspend fun recordPlayed(identity: TrackIdentity, atMs: Long)

    suspend fun historyTracks(limit: Int): List<Track>

    suspend fun mostPlayedTracks(limit: Int): List<Track>

    // ----- Session persistence / restore (§10.3) -----
    suspend fun saveSession(session: PersistedSession)

    suspend fun loadSession(): PersistedSession?
}

/** Persisted playback session for cold-start restore (into PAUSED, never auto-play). */
data class PersistedSession(
    val queueIdentities: List<TrackIdentity>,
    val index: Int,
    val positionMs: Long,
    val shuffle: Boolean,
    val repeatOrdinal: Int,
    val speed: Float,
)
