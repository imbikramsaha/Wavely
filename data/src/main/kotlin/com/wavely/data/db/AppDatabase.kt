package com.wavely.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wavely.data.db.dao.FavoriteDao
import com.wavely.data.db.dao.PlaylistDao
import com.wavely.data.db.dao.SessionDao
import com.wavely.data.db.dao.StatsDao
import com.wavely.data.db.entity.FavoriteEntity
import com.wavely.data.db.entity.HistoryEntity
import com.wavely.data.db.entity.PlayCountEntity
import com.wavely.data.db.entity.PlaylistEntity
import com.wavely.data.db.entity.PlaylistTrackEntity
import com.wavely.data.db.entity.SessionQueueEntity
import com.wavely.data.db.entity.SessionStateEntity

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
        FavoriteEntity::class,
        PlayCountEntity::class,
        HistoryEntity::class,
        SessionQueueEntity::class,
        SessionStateEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun statsDao(): StatsDao
    abstract fun sessionDao(): SessionDao

    companion object {
        const val NAME = "listn.db"
    }
}
