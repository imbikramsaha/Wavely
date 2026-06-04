package com.wavely.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wavely.data.db.entity.HistoryEntity
import com.wavely.data.db.entity.PlayCountEntity

@Dao
interface StatsDao {

    @Insert
    suspend fun insertHistory(entry: HistoryEntity)

    /** Keep only the [keep] most-recent rows so the append-only history can't grow unbounded. */
    @Query("DELETE FROM history WHERE id NOT IN (SELECT id FROM history ORDER BY playedAtMs DESC LIMIT :keep)")
    suspend fun pruneHistory(keep: Int)

    @Query("SELECT trackKey FROM history ORDER BY playedAtMs DESC LIMIT :limit")
    suspend fun recentKeys(limit: Int): List<String>

    @Query("SELECT * FROM play_counts WHERE trackKey = :trackKey")
    suspend fun playCount(trackKey: String): PlayCountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlayCount(entity: PlayCountEntity)

    @Query("SELECT trackKey FROM play_counts ORDER BY count DESC, lastPlayedAtMs DESC LIMIT :limit")
    suspend fun mostPlayedKeys(limit: Int): List<String>
}
