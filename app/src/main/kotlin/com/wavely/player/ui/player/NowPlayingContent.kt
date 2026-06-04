package com.wavely.player.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wavely.domain.model.PlaybackUiState
import com.wavely.player.R
import com.wavely.player.ui.components.AlbumArt
import com.wavely.player.ui.components.Seekbar
import com.wavely.player.ui.components.TransportControls
import com.wavely.player.ui.components.WavelyAmbientBackground
import com.wavely.player.ui.theme.Spacing
import com.wavely.player.ui.util.formatSpeed
import com.wavely.player.ui.util.secondaryLine

/** Speed cycle used by the utility row (design.md §2.2 default 1.0x). */
private val SPEED_STEPS = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

fun nextSpeed(current: Float): Float {
    val idx = SPEED_STEPS.indexOfFirst { it >= current - 0.001f }.takeIf { it >= 0 } ?: SPEED_STEPS.indexOf(1.0f)
    return SPEED_STEPS[(idx + 1) % SPEED_STEPS.size]
}

// Now Playing is an immersive dark surface (art-derived ambient), so its controls are always
// light regardless of the app theme.
internal val NpPrimaryText = Color(0xFFFFFFFF)
internal val NpSecondaryText = Color(0xB3FFFFFF)   // white 70%
internal val NpControlInactive = Color(0xC7FFFFFF) // white 78%
internal val NpTrack = Color(0x38FFFFFF)           // white 22%

/** All the controls bundled, used by both the portrait sheet and landscape two-pane callbacks. */
data class NowPlayingCallbacks(
    val onPlayPause: () -> Unit,
    val onNext: () -> Unit,
    val onPrevious: () -> Unit,
    val onSeek: (Long) -> Unit,
    val onShuffle: () -> Unit,
    val onRepeat: () -> Unit,
    val onSpeed: () -> Unit,
    val onSleep: () -> Unit,
    val onToggleFavorite: () -> Unit,
    val onShowQueue: () -> Unit,
)

/**
 * Now Playing content — portrait sheet (design.md §2.2): drag handle, big art, then controls,
 * over the track's art-derived ambient background. The landscape fork (§8.1) is
 * [NowPlayingTwoPane], which reuses [NowPlayingControls].
 */
@Composable
fun NowPlayingContent(
    state: PlaybackUiState,
    isFavorite: Boolean,
    callbacks: NowPlayingCallbacks,
    modifier: Modifier = Modifier,
    showDragHandle: Boolean = true,
) {
    val track = state.current ?: return
    Box(modifier = modifier.fillMaxSize()) {
        WavelyAmbientBackground(title = track.title, modifier = Modifier.fillMaxSize())
        // legibility scrim
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0x4C080A0E), Color(0xB8080A0E))),
            ),
        )
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val artSide = minOf(maxWidth * 0.78f, (maxHeight - 340.dp).coerceAtLeast(48.dp), 360.dp)
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (showDragHandle) DragHandle() else Spacer(Modifier.height(Spacing.md))
                Spacer(Modifier.weight(1f))
                AlbumArt(
                    uri = track.albumArtUri,
                    seed = track.title,
                    cornerRadius = 24.dp,
                    modifier = Modifier
                        .size(artSide)
                        .shadow(28.dp, RoundedCornerShape(24.dp), clip = false),
                )
                Spacer(Modifier.height(Spacing.lg))
                NowPlayingControls(
                    state = state,
                    isFavorite = isFavorite,
                    callbacks = callbacks,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.weight(1.6f))
            }
        }
    }
}

/** Title + artist + transport + seekbar + utility row, without the album art. Shared. */
@Composable
fun NowPlayingControls(
    state: PlaybackUiState,
    isFavorite: Boolean,
    callbacks: NowPlayingCallbacks,
    modifier: Modifier = Modifier,
) {
    val track = state.current ?: return
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = track.title,
            style = MaterialTheme.typography.titleLarge,
            color = NpPrimaryText,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = secondaryLine(track.artist, track.album),
            style = MaterialTheme.typography.titleSmall,
            color = NpSecondaryText,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(Spacing.xl))
        TransportControls(
            isPlaying = state.isPlaying,
            shuffle = state.shuffle,
            repeat = state.repeat,
            onShuffle = callbacks.onShuffle,
            onPrevious = callbacks.onPrevious,
            onPlayPause = callbacks.onPlayPause,
            onNext = callbacks.onNext,
            onRepeat = callbacks.onRepeat,
        )
        Spacer(Modifier.height(Spacing.md))
        Seekbar(positionMs = state.positionMs, durationMs = state.durationMs, onSeek = callbacks.onSeek)
        Spacer(Modifier.height(Spacing.sm))
        UtilityRow(
            speed = state.speed,
            sleepActive = state.sleepTimerEndMs != null,
            isFavorite = isFavorite,
            onShowQueue = callbacks.onShowQueue,
            onSleep = callbacks.onSleep,
            onSpeed = callbacks.onSpeed,
            onToggleFavorite = callbacks.onToggleFavorite,
        )
    }
}

@Composable
private fun DragHandle() {
    Box(
        modifier = Modifier
            .padding(top = Spacing.sm)
            .size(width = 36.dp, height = 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0x80FFFFFF)),
    )
}

@Composable
private fun UtilityRow(
    speed: Float,
    sleepActive: Boolean,
    isFavorite: Boolean,
    onShowQueue: () -> Unit,
    onSleep: () -> Unit,
    onSpeed: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary
    val inactive = NpControlInactive
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onShowQueue, modifier = Modifier.size(48.dp)) {
            Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = stringResource(R.string.tab_queue), tint = inactive)
        }
        IconButton(onClick = onSleep, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Filled.Bedtime, contentDescription = "Sleep timer", tint = if (sleepActive) accent else inactive)
        }
        IconButton(
            onClick = onSpeed,
            modifier = Modifier
                .size(48.dp)
                .semantics { contentDescription = "Playback speed, ${formatSpeed(speed)}" },
        ) {
            Text(
                text = formatSpeed(speed),
                style = MaterialTheme.typography.labelLarge,
                color = if (speed != 1.0f) accent else inactive,
                modifier = Modifier.clearAndSetSemantics {},
            )
        }
        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = stringResource(if (isFavorite) R.string.cd_unfavorite else R.string.cd_favorite),
                tint = if (isFavorite) accent else inactive,
            )
        }
    }
}
