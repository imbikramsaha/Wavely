package com.wavely.player.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wavely.domain.model.PlaybackUiState
import com.wavely.player.ui.components.AlbumArt
import com.wavely.player.ui.components.WavelyAmbientBackground
import com.wavely.player.ui.theme.Spacing

/**
 * Landscape Now Playing — full-screen two-pane (design.md §8.1, risk T-5).
 * NOT a bottom sheet: art on the left, controls on the right, over the art-derived ambient.
 * Reached via the same tap/swipe on the mini-player; collapsed by back / swipe / the affordance.
 */
@Composable
fun NowPlayingTwoPane(
    state: PlaybackUiState,
    isFavorite: Boolean,
    callbacks: NowPlayingCallbacks,
    modifier: Modifier = Modifier,
) {
    val track = state.current ?: return
    Box(modifier = modifier.fillMaxSize()) {
        WavelyAmbientBackground(title = track.title, modifier = Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0x4C080A0E), Color(0xB8080A0E))),
            ),
        )
        Row(modifier = Modifier.fillMaxSize().padding(Spacing.lg)) {
            Box(
                modifier = Modifier.fillMaxHeight().weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                AlbumArt(
                    uri = track.albumArtUri,
                    seed = track.title,
                    cornerRadius = 20.dp,
                    modifier = Modifier
                        .fillMaxHeight(0.9f)
                        .aspectRatio(1f)
                        .shadow(24.dp, RoundedCornerShape(20.dp), clip = false),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = Spacing.lg),
                verticalArrangement = Arrangement.Center,
            ) {
                NowPlayingControls(
                    state = state,
                    isFavorite = isFavorite,
                    callbacks = callbacks,
                    modifier = Modifier,
                )
            }
        }
    }
}
