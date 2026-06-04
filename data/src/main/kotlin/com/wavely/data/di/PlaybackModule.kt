package com.wavely.data.di

import com.wavely.data.playback.TrackPlaybackReporter
import com.wavely.domain.repository.PlaylistRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlaybackModule {

    /**
     * Records play history / counts when a new track starts. Lives here so the
     * playback layer ([com.wavely.data.playback.PlaybackConnection]) stays free of
     * a Room dependency. Fire-and-forget on the app scope.
     */
    @Provides
    @Singleton
    fun provideTrackPlaybackReporter(
        playlistRepository: PlaylistRepository,
        scope: CoroutineScope,
    ): TrackPlaybackReporter = TrackPlaybackReporter { track ->
        scope.launch {
            playlistRepository.recordPlayed(track.identity, System.currentTimeMillis())
        }
    }
}
