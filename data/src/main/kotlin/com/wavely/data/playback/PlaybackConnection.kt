package com.wavely.data.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.wavely.domain.model.PlaybackUiState
import com.wavely.domain.model.RepeatMode
import com.wavely.domain.model.Track
import com.wavely.domain.repository.PlaybackController
import com.wavely.domain.usecase.PreviousTrackRule
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Binds asynchronously to [PlaybackService] via a [MediaController] and projects
 * Media3 player state into a single [PlaybackUiState] StateFlow. Leans on Media3's
 * built-in shuffle order rather than hand-rolling a shuffle map (design.md risk T-4).
 *
 * The pre-connection window is surfaced via [PlaybackUiState.isConnecting] so the
 * mini-player renders a neutral, non-interactive state (risk T-3).
 */
@Singleton
class PlaybackConnection @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scope: CoroutineScope,
    private val onTrackStarted: TrackPlaybackReporter,
) : PlaybackController {

    private val _state = MutableStateFlow(PlaybackUiState.Empty)
    override val state: StateFlow<PlaybackUiState> = _state.asStateFlow()

    private val _errorEvents = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 4,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val errorEvents: Flow<String> = _errorEvents.asSharedFlow()

    private var controller: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var positionJob: Job? = null
    private var sleepJob: Job? = null

    /** True while the host UI is visible — gates the position ticker for battery (§10.1). */
    @Volatile private var uiActive: Boolean = true

    private var lastReportedKey: String? = null

    init {
        connect()
    }

    private fun connect() {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        controllerFuture = future
        future.addListener({
            val c = future.get()
            controller = c
            c.addListener(PlayerListener())
            pushState()
            _state.update { it.copy(isConnecting = false) }
            maybeStartTicker()
        }, MoreExecutors.directExecutor())
    }

    fun setUiActive(active: Boolean) {
        uiActive = active
        maybeStartTicker()
    }

    fun release() {
        positionJob?.cancel()
        sleepJob?.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
    }

    // ----- Commands -----

    override fun playQueue(tracks: List<Track>, startIndex: Int) {
        val c = controller ?: return
        c.setMediaItems(tracks.map(TrackMediaMapper::toMediaItem), startIndex.coerceIn(0, maxOf(0, tracks.size - 1)), 0L)
        c.prepare()
        c.playWhenReady = true
    }

    override fun restoreSession(
        tracks: List<Track>,
        index: Int,
        positionMs: Long,
        shuffle: Boolean,
        repeat: RepeatMode,
        speed: Float,
    ) {
        val c = controller ?: return
        if (tracks.isEmpty() || c.mediaItemCount > 0) return // don't clobber an active queue
        c.setMediaItems(tracks.map(TrackMediaMapper::toMediaItem), index.coerceIn(0, tracks.lastIndex), positionMs)
        c.shuffleModeEnabled = shuffle
        setRepeatMode(repeat)
        c.setPlaybackSpeed(speed)
        c.prepare()
        c.playWhenReady = false // restore into PAUSED, never auto-play (§10.3)
    }

    override fun play() { controller?.play() }

    override fun pause() { controller?.pause() }

    override fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    override fun next() { controller?.seekToNext() }

    override fun previous() {
        val c = controller ?: return
        when (PreviousTrackRule.decide(c.currentPosition, c.hasPreviousMediaItem())) {
            PreviousTrackRule.Action.RestartCurrent -> c.seekTo(0)
            PreviousTrackRule.Action.PreviousTrack -> c.seekToPreviousMediaItem()
        }
    }

    override fun seekTo(positionMs: Long) { controller?.seekTo(positionMs) }

    override fun setShuffle(enabled: Boolean) { controller?.shuffleModeEnabled = enabled }

    override fun setRepeatMode(mode: RepeatMode) {
        controller?.repeatMode = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
    }

    override fun setSpeed(speed: Float) { controller?.setPlaybackSpeed(speed) }

    override fun addToQueue(tracks: List<Track>) {
        controller?.addMediaItems(tracks.map(TrackMediaMapper::toMediaItem))
    }

    override fun playNext(tracks: List<Track>) {
        val c = controller ?: return
        val insertIndex = (c.currentMediaItemIndex + 1).coerceIn(0, c.mediaItemCount)
        c.addMediaItems(insertIndex, tracks.map(TrackMediaMapper::toMediaItem))
    }

    override fun removeFromQueue(index: Int) {
        val c = controller ?: return
        if (index in 0 until c.mediaItemCount) c.removeMediaItem(index)
    }

    override fun insertIntoQueue(index: Int, tracks: List<Track>) {
        val c = controller ?: return
        val at = index.coerceIn(0, c.mediaItemCount)
        c.addMediaItems(at, tracks.map(TrackMediaMapper::toMediaItem))
    }

    override fun moveQueueItem(from: Int, to: Int) {
        val c = controller ?: return
        if (from in 0 until c.mediaItemCount && to in 0 until c.mediaItemCount) c.moveMediaItem(from, to)
    }

    override fun skipToQueueIndex(index: Int) {
        val c = controller ?: return
        if (index in 0 until c.mediaItemCount) c.seekToDefaultPosition(index)
    }

    override fun setSleepTimer(endMs: Long?) {
        sleepJob?.cancel()
        // A previous timer may have been cancelled mid-fade; make sure volume is back to full.
        controller?.let { if (it.volume < 1f) it.volume = 1f }
        _state.update { it.copy(sleepTimerEndMs = endMs) }
        if (endMs == null) return
        sleepJob = scope.launch {
            val remaining = endMs - System.currentTimeMillis()
            // Hold full volume until the last few seconds, then ease down so playback doesn't
            // cut out abruptly at night. Volume is restored after pausing so the next play isn't
            // silent.
            if (remaining > SLEEP_FADE_MS) delay(remaining - SLEEP_FADE_MS)
            val c = controller
            if (c != null) {
                val startVolume = c.volume.takeIf { it > 0f } ?: 1f
                val fadeWindow = minOf(remaining, SLEEP_FADE_MS).coerceAtLeast(0L)
                repeat(SLEEP_FADE_STEPS) { step ->
                    delay(fadeWindow / SLEEP_FADE_STEPS)
                    c.volume = startVolume * (1f - (step + 1) / SLEEP_FADE_STEPS.toFloat())
                }
                c.pause()
                c.volume = startVolume
            }
            _state.update { it.copy(sleepTimerEndMs = null) }
        }
    }

    // ----- State projection -----

    private inner class PlayerListener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            pushState()
            maybeStartTicker()
            reportIfTrackChanged(player)
        }

        /**
         * A track failed to decode/open (corrupt, unsupported, or deleted by another app while
         * queued). Skip it and tell the user, instead of stalling. Design.md §11 Stage 6.
         */
        override fun onPlayerError(error: PlaybackException) {
            val c = controller ?: return
            val title = c.currentMediaItem?.mediaMetadata?.title?.toString()
            _errorEvents.tryEmit(if (title != null) "Can't play \"$title\" — skipping" else "Can't play this track — skipping")
            if (c.hasNextMediaItem()) {
                c.seekToNext()
                c.prepare()
                c.play()
            } else {
                c.stop()
            }
        }
    }

    private fun reportIfTrackChanged(player: Player) {
        val key = player.currentMediaItem?.mediaId
        if (key != null && player.isPlaying && key != lastReportedKey) {
            lastReportedKey = key
            val track = TrackMediaMapper.fromMediaItem(player.currentMediaItem)
            if (track != null) onTrackStarted.report(track)
        }
    }

    private fun pushState() {
        val c = controller ?: return
        val queue = (0 until c.mediaItemCount).mapNotNull { i ->
            TrackMediaMapper.fromMediaItem(c.getMediaItemAt(i))
        }
        val repeat = when (c.repeatMode) {
            Player.REPEAT_MODE_ONE -> RepeatMode.ONE
            Player.REPEAT_MODE_ALL -> RepeatMode.ALL
            else -> RepeatMode.OFF
        }
        _state.update { prev ->
            prev.copy(
                current = TrackMediaMapper.fromMediaItem(c.currentMediaItem),
                queue = queue,
                queueIndex = c.currentMediaItemIndex,
                isPlaying = c.isPlaying,
                positionMs = c.currentPosition.coerceAtLeast(0),
                durationMs = c.duration.takeIf { it > 0 } ?: 0L,
                shuffle = c.shuffleModeEnabled,
                repeat = repeat,
                speed = c.playbackParameters.speed,
                isConnecting = false,
            )
        }
    }

    private fun maybeStartTicker() {
        val c = controller
        val shouldTick = c != null && c.isPlaying && uiActive
        if (shouldTick && positionJob?.isActive != true) {
            positionJob = scope.launch {
                while (true) {
                    val cur = controller ?: break
                    _state.update { it.copy(positionMs = cur.currentPosition.coerceAtLeast(0)) }
                    delay(POSITION_TICK_MS)
                    if (!cur.isPlaying || !uiActive) break
                }
            }
        } else if (!shouldTick) {
            positionJob?.cancel()
            positionJob = null
        }
    }

    companion object {
        private const val POSITION_TICK_MS = 400L

        /** Sleep timer: ease the volume down over the final stretch instead of cutting abruptly. */
        private const val SLEEP_FADE_MS = 8_000L
        private const val SLEEP_FADE_STEPS = 20
    }
}

/** Decouples history/play-count recording from the playback layer (no Room dependency here). */
fun interface TrackPlaybackReporter {
    fun report(track: Track)
}
