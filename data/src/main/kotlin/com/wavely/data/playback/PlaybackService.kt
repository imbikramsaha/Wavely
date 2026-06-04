package com.wavely.data.playback

import android.app.PendingIntent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Foreground [MediaSessionService] hosting the ExoPlayer. The UI never binds to
 * this directly — it connects through a MediaController (see [PlaybackConnection]).
 * Media3 supplies the media-style notification + lock-screen controls automatically.
 * See design.md §10.4.
 */
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject lateinit var playerProvider: PlayerProvider

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = playerProvider.create(this)
        val builder = MediaSession.Builder(this, player)
        // Tapping the media notification / lock-screen art should open the app. Resolve the
        // launcher activity by package (this module must not depend on :app), so the system has
        // a destination to navigate to instead of doing nothing.
        sessionActivityIntent()?.let { builder.setSessionActivity(it) }
        mediaSession = builder.build()
    }

    private fun sessionActivityIntent(): PendingIntent? {
        val launch = packageManager.getLaunchIntentForPackage(packageName) ?: return null
        return PendingIntent.getActivity(
            this,
            0,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        // If the user swipes the app away while paused, stop the service.
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}

/**
 * Creates a configured ExoPlayer. Centralizes audio-focus + becoming-noisy setup
 * so the service and tests share one definition (design.md §10.4).
 */
class PlayerProvider @Inject constructor() {
    fun create(service: PlaybackService): ExoPlayer {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
        return ExoPlayer.Builder(service)
            // handleAudioFocus = true → transient ducks, permanent pauses + abandons.
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            // Pause when headphones unplugged / output becomes noisy.
            .setHandleAudioBecomingNoisy(true)
            .build()
    }
}
