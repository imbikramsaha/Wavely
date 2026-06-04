package com.wavely.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wavely.domain.model.RepeatMode
import com.wavely.player.R
import com.wavely.player.ui.theme.brandGradient

/** Now Playing transport row — design.md §2.2 (music-standard, not podcast skip). */
@Composable
fun TransportControls(
    isPlaying: Boolean,
    shuffle: Boolean,
    repeat: RepeatMode,
    onShuffle: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onRepeat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Now Playing is an immersive dark ambient surface → controls are always light.
    val accent = MaterialTheme.colorScheme.primary
    val inactive = Color(0xC7FFFFFF) // white 78%
    val onArt = Color.White

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onShuffle, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.Filled.Shuffle,
                contentDescription = stringResource(R.string.cd_shuffle),
                tint = if (shuffle) accent else inactive,
                modifier = Modifier.size(24.dp),
            )
        }
        IconButton(onClick = onPrevious, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = stringResource(R.string.cd_previous),
                tint = onArt,
                modifier = Modifier.size(32.dp),
            )
        }
        // Dominant play/pause inside a 72dp brand-gradient circle.
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(brandGradient),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(onClick = onPlayPause, modifier = Modifier.size(72.dp)) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = stringResource(if (isPlaying) R.string.cd_pause else R.string.cd_play),
                    tint = Color.White,
                    modifier = Modifier.size(34.dp),
                )
            }
        }
        IconButton(onClick = onNext, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = stringResource(R.string.cd_next),
                tint = onArt,
                modifier = Modifier.size(32.dp),
            )
        }
        IconButton(onClick = onRepeat, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = if (repeat == RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                contentDescription = stringResource(R.string.cd_repeat),
                tint = if (repeat == RepeatMode.OFF) inactive else accent,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
