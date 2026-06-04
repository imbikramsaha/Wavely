package com.wavely.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.wavely.data.db.entity.PlaylistEntity
import com.wavely.data.db.entity.PlaylistTrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Query("SELECT * FROM playlists ORDER BY name COLLATE NOCASE ASC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT COUNT(*) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun trackCount(playlistId: Long): Int

    @Query("SELECT trackKey FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY position ASC")
    suspend fun trackKeys(playlistId: Long): List<String>

    @Query("SELECT MAX(position) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun maxPosition(playlistId: Long): Int?

    @Insert
    suspend fun insertTracks(tracks: List<PlaylistTrackEntity>)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackKey = :trackKey")
    suspend fun removeTrack(playlistId: Long, trackKey: String)

    @Query("SELECT * FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY position ASC")
    suspend fun rows(playlistId: Long): List<PlaylistTrackEntity>

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun clearTracks(playlistId: Long)

    /** Rewrite all rows with contiguous positions — used by add/remove/reorder. */
    @Transaction
    suspend fun rewrite(playlistId: Long, orderedKeys: List<String>) {
        clearTracks(playlistId)
        insertTracks(
            orderedKeys.mapIndexed { index, key ->
                PlaylistTrackEntity(playlistId = playlistId, trackKey = key, position = index)
            },
        )
    }
}
