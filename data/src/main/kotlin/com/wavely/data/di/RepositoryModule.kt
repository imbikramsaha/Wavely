package com.wavely.data.di

import com.wavely.data.playback.PlaybackConnection
import com.wavely.data.repository.MediaRepositoryImpl
import com.wavely.data.repository.PlaylistRepositoryImpl
import com.wavely.data.settings.SettingsRepositoryImpl
import com.wavely.domain.repository.MediaRepository
import com.wavely.domain.repository.PlaybackController
import com.wavely.domain.repository.PlaylistRepository
import com.wavely.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMediaRepository(impl: MediaRepositoryImpl): MediaRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(impl: PlaylistRepositoryImpl): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindPlaybackController(impl: PlaybackConnection): PlaybackController
}
