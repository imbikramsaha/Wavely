package com.wavely.data.di

import android.content.Context
import androidx.room.Room
import com.wavely.data.db.AppDatabase
import com.wavely.data.db.dao.FavoriteDao
import com.wavely.data.db.dao.PlaylistDao
import com.wavely.data.db.dao.SessionDao
import com.wavely.data.db.dao.StatsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun providePlaylistDao(db: AppDatabase): PlaylistDao = db.playlistDao()
    @Provides fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun provideStatsDao(db: AppDatabase): StatsDao = db.statsDao()
    @Provides fun provideSessionDao(db: AppDatabase): SessionDao = db.sessionDao()
}
