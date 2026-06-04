package com.wavely.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wavely.domain.model.PlaybackUiState
import com.wavely.player.R
import com.wavely.player.ui.theme.Dimens
import com.wavely.player.ui.theme.LocalWavelyColors
import com.wavely.player.ui.theme.Spacing
import com.wavely.player.ui.theme.brandGradient
import com.wavely.player.ui.util.secondaryLine

/**
 * Persistent mini-player strip — v2: an elevated surface bar with a cyan→indigo gradient
 * progress hairline on its top edge and the playing-art equalizer. During the pre-connection
 * window it renders neutral and non-interactive (risk T-3).
 */
@Composable
fun MiniPlayer(
    state: PlaybackUiState,
    onTap: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val track = state.current
    val interactive = !state.isConnecting && track != null
    val progress = if (state.durationMs > 0) {
        (state.positionMs.toFloat() / state.durationMs).coerceIn(0f, 1f)
    } else {
        0f
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.miniPlayerHeight)
                .then(if (interactive) Modifier.clickable(onClickLabel = "Open now playing", onClick = onTap) else Modifier)
                .padding(start = Spacing.sm, end = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val art = Modifier.size(Dimens.miniThumbnail)
            when {
                interactive && state.isPlaying -> PlayingArt(title = track!!.title, modifier = art, cornerRadius = 11.dp)
                else -> AlbumArt(uri = track?.albumArtUri, seed = track?.title, modifier = art, cornerRadius = 11.dp)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Spacing.sm),
            ) {
                Text(
                    text = track?.title ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (track != null) secondaryLine(track.artist, track.album) else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (interactive) {
                IconButton(onClick = onPlayPause, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = stringResource(if (state.isPlaying) R.string.cd_pause else R.string.cd_play),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                IconButton(onClick = onNext, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = stringResource(R.string.cd_next),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        // gradient progress hairline along the top edge
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .height(2.5.dp)
                .background(LocalWavelyColors.current.line),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(brandGradient),
            )
        }
    }
}
