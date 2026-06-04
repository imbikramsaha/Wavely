package com.wavely.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Single, unqualified bindings:
 *  - [CoroutineDispatcher] = IO, injected by repositories for `withContext` off the main thread.
 *  - [CoroutineScope] = a Main-immediate app scope, injected by [com.wavely.data.playback.PlaybackConnection]
 *    (the MediaController must be driven from the main thread).
 */
@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {

    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
}
