package com.wavely.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wavely.data.db.entity.SessionQueueEntity
import com.wavely.data.db.entity.SessionStateEntity

@Dao
interface SessionDao {

    @Query("DELETE FROM session_queue")
    suspend fun clearQueue()

    @Insert
    suspend fun insertQueue(rows: List<SessionQueueEntity>)

    @Query("SELECT trackKey FROM session_queue ORDER BY position ASC")
    suspend fun queueKeys(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertState(state: SessionStateEntity)

    @Query("SELECT * FROM session_state WHERE id = 0")
    suspend fun state(): SessionStateEntity?

    @Transaction
    suspend fun saveSession(orderedKeys: List<String>, state: SessionStateEntity) {
        clearQueue()
        insertQueue(orderedKeys.mapIndexed { i, key -> SessionQueueEntity(position = i, trackKey = key) })
        upsertState(state)
    }
}
