package com.wavely.data.repository

import com.wavely.data.db.dao.FavoriteDao
import com.wavely.data.db.dao.PlaylistDao
import com.wavely.data.db.dao.SessionDao
import com.wavely.data.db.dao.StatsDao
import com.wavely.data.db.entity.FavoriteEntity
import com.wavely.data.db.entity.HistoryEntity
import com.wavely.data.db.entity.PlayCountEntity
import com.wavely.data.db.entity.PlaylistEntity
import com.wavely.data.db.entity.SessionStateEntity
import com.wavely.domain.model.Playlist
import com.wavely.domain.model.Track
import com.wavely.domain.model.TrackIdentity
import com.wavely.domain.repository.MediaRepository
import com.wavely.domain.repository.PersistedSession
import com.wavely.domain.repository.PlaylistRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val favoriteDao: FavoriteDao,
    private val statsDao: StatsDao,
    private val sessionDao: SessionDao,
    private val mediaRepository: MediaRepository,
    private val io: CoroutineDispatcher,
) : PlaylistRepository {

    // Serializes read-modify-write mutations on a playlist's ordered keys. A drag-reorder fires
    // many overlapping reorder() calls; without this, two coroutines could each read the keys
    // before the other writes, and the last write would clobber the earlier move (wrong order
    // persisted). The Mutex is fair (FIFO), so moves apply in the same order the UI saw them.
    private val playlistMutation = Mutex()

    // ----- Playlists -----
    override fun observePlaylists(): Flow<List<Playlist>> =
        playlistDao.observePlaylists().map { entities ->
            entities.map { e ->
                val keys = playlistDao.trackKeys(e.id)
                val arts = resolve(keys).take(4).mapNotNull { it.albumArtUri }
                Playlist(id = e.id, name = e.name, trackCount = keys.size, artUris = arts)
            }
        }

    override suspend fun createPlaylist(name: String): Long = withContext(io) {
        playlistDao.insertPlaylist(PlaylistEntity(name = name, createdAtMs = now()))
    }

    override suspend fun renamePlaylist(playlistId: Long, name: String) = withContext(io) {
        playlistDao.rename(playlistId, name)
    }

    override suspend fun deletePlaylist(playlistId: Long) = withContext(io) {
        playlistDao.deletePlaylist(playlistId)
    }

    override suspend fun addToPlaylist(playlistId: Long, identities: List<TrackIdentity>) = withContext(io) {
        playlistMutation.withLock {
            val existing = playlistDao.trackKeys(playlistId).toMutableList()
            identities.forEach { id ->
                val key = id.asKey()
                if (key !in existing) existing += key
            }
            playlistDao.rewrite(playlistId, existing)
        }
    }

    override suspend fun removeFromPlaylist(playlistId: Long, identity: TrackIdentity) = withContext(io) {
        playlistMutation.withLock {
            playlistDao.removeTrack(playlistId, identity.asKey())
            // Re-pack positions so they stay contiguous.
            playlistDao.rewrite(playlistId, playlistDao.trackKeys(playlistId))
        }
    }

    override suspend fun reorderPlaylist(playlistId: Long, from: Int, to: Int) = withContext(io) {
        playlistMutation.withLock {
            val keys = playlistDao.trackKeys(playlistId).toMutableList()
            if (from in keys.indices && to in keys.indices) {
                keys.add(to, keys.removeAt(from))
                playlistDao.rewrite(playlistId, keys)
            }
        }
    }

    override suspend fun playlistTracks(playlistId: Long): List<Track> = withContext(io) {
        resolve(playlistDao.trackKeys(playlistId))
    }

    // ----- Favorites -----
    override fun observeFavorites(): Flow<Set<String>> =
        favoriteDao.observeKeys().map { it.toSet() }

    override suspend fun setFavorite(identity: TrackIdentity, favorite: Boolean) = withContext(io) {
        if (favorite) favoriteDao.add(FavoriteEntity(identity.asKey(), now()))
        else favoriteDao.remove(identity.asKey())
    }

    override suspend fun favoriteTracks(): List<Track> = withContext(io) {
        resolve(favoriteDao.keys())
    }

    // ----- History & counts -----
    override suspend fun recordPlayed(identity: TrackIdentity, atMs: Long) = withContext(io) {
        val key = identity.asKey()
        statsDao.insertHistory(HistoryEntity(trackKey = key, playedAtMs = atMs))
        // History is append-only and currently has no screen reading it (Wavely v2). Cap it so the
        // table can't grow forever. play_counts is naturally bounded (one row per track), so only
        // history needs pruning.
        statsDao.pruneHistory(HISTORY_CAP)
        val prev = statsDao.playCount(key)
        statsDao.upsertPlayCount(
            PlayCountEntity(trackKey = key, count = (prev?.count ?: 0) + 1, lastPlayedAtMs = atMs),
        )
    }

    override suspend fun historyTracks(limit: Int): List<Track> = withContext(io) {
        // recentKeys can repeat a track; preserve recency order, de-dup.
        resolveOrdered(statsDao.recentKeys(limit).distinct())
    }

    override suspend fun mostPlayedTracks(limit: Int): List<Track> = withContext(io) {
        resolveOrdered(statsDao.mostPlayedKeys(limit))
    }

    // ----- Session -----
    override suspend fun saveSession(session: PersistedSession) = withContext(io) {
        sessionDao.saveSession(
            orderedKeys = session.queueIdentities.map { it.asKey() },
            state = SessionStateEntity(
                index = session.index,
                positionMs = session.positionMs,
                shuffle = session.shuffle,
                repeatOrdinal = session.repeatOrdinal,
                speed = session.speed,
            ),
        )
    }

    override suspend fun loadSession(): PersistedSession? = withContext(io) {
        val state = sessionDao.state() ?: return@withContext null
        val keys = sessionDao.queueKeys()
        val identities = keys.mapNotNull { TrackIdentity.fromKey(it) }
        PersistedSession(
            queueIdentities = identities,
            index = state.index,
            positionMs = state.positionMs,
            shuffle = state.shuffle,
            repeatOrdinal = state.repeatOrdinal,
            speed = state.speed,
        )
    }

    // ----- helpers -----

    /** Resolve identity keys → tracks (order not guaranteed; unresolved dropped). */
    private suspend fun resolve(keys: List<String>): List<Track> {
        val identities = keys.mapNotNull { TrackIdentity.fromKey(it) }
        return mediaRepository.resolveTracks(identities)
    }

    /** Resolve keys preserving the input order. */
    private suspend fun resolveOrdered(keys: List<String>): List<Track> {
        val resolved = resolve(keys).associateBy { it.identity.asKey() }
        return keys.mapNotNull { resolved[it] }
    }

    private fun now(): Long = System.currentTimeMillis()

    private companion object {
        /** Upper bound on retained play-history rows (recency-ordered). */
        const val HISTORY_CAP = 500
    }
}
