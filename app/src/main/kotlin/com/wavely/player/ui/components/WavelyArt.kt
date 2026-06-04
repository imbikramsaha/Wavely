package com.wavely.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.wavely.player.ui.theme.Dimens
import com.wavely.player.ui.theme.wvArtColors
import com.wavely.player.ui.theme.wvFallbackBrush
import com.wavely.player.ui.theme.wvMonogram

/**
 * Generated gradient fallback artwork for an art-less track (v2 design): a title-hashed
 * diagonal gradient tile with a soft top sheen and the track's monogram. The footprint comes
 * from [modifier] (e.g. `Modifier.size(52.dp)` or `fillMaxSize()`); the monogram auto-scales to
 * the measured size, so callers never thread a size through.
 */
@Composable
fun WavelyArt(
    title: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = Dimens.cornerTile,
    showMonogram: Boolean = true,
    dim: Boolean = false,
) {
    val brush = remember(title) { wvFallbackBrush(title) }
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
            .alpha(if (dim) 0.5f else 1f),
        contentAlignment = Alignment.Center,
    ) {
        // soft top sheen
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0x3DFFFFFF), Color.Transparent))),
        )
        if (showMonogram) {
            Text(
                text = wvMonogram(title),
                color = Color(0xF2FFFFFF),
                fontWeight = FontWeight.Bold,
                fontSize = (maxWidth.value * 0.42f).sp,
                fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
            )
        }
    }
}

/**
 * Active-track artwork: the gradient tile dimmed under a soft scrim with a white animated
 * equalizer on top (v2 design `ArtPlaying`). Used by the playing row + mini-player.
 */
@Composable
fun PlayingArt(
    title: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = Dimens.cornerTile,
) {
    Box(modifier = modifier.clip(RoundedCornerShape(cornerRadius)), contentAlignment = Alignment.Center) {
        WavelyArt(title = title, modifier = Modifier.fillMaxSize(), cornerRadius = cornerRadius, showMonogram = false, dim = true)
        Box(Modifier.fillMaxSize().background(Color(0x47000000)))
        AnimatedEqualizer(color = Color.White)
    }
}

/**
 * Art-derived ambient background for the Now Playing surface: a top wash → deep middle → bottom
 * wash of the track's tile colours (size-independent). Port of art.jsx `wvAmbient`.
 */
@Composable
fun WavelyAmbientBackground(title: String, modifier: Modifier = Modifier) {
    val brush = remember(title) {
        val (a, b) = wvArtColors(title)
        val base = Color(0xFF0A0C10)
        Brush.verticalGradient(
            0f to a.copy(alpha = 0.45f),
            0.42f to base,
            0.72f to base,
            1f to b.copy(alpha = 0.42f),
        )
    }
    Box(modifier = modifier.fillMaxSize().background(brush))
}
