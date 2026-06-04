package com.wavely.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wavely.data.db.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE trackKey = :trackKey")
    suspend fun remove(trackKey: String)

    @Query("SELECT trackKey FROM favorites")
    fun observeKeys(): Flow<List<String>>

    @Query("SELECT trackKey FROM favorites ORDER BY addedAtMs DESC")
    suspend fun keys(): List<String>
}
