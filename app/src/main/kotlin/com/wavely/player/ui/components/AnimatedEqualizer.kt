package com.wavely.player.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wavely.player.ui.theme.LocalWavelyColors

/**
 * Four-bar animated equalizer that marks the currently-playing track (v2 design). Bars
 * bob between ~35% and their peak on staggered cycles. Tinted with the brand equalizer
 * colour by default.
 */
@Composable
fun AnimatedEqualizer(
    modifier: Modifier = Modifier,
    color: Color = LocalWavelyColors.current.eq,
) {
    val transition = rememberInfiniteTransition(label = "eq")
    // (peak height fraction, phase offset ms) per bar — mirrors the CSS keyframe delays.
    val bars = listOf(0.40f to 200, 0.90f to 540, 0.65f to 100, 1.00f to 380)
    Row(
        modifier = modifier.height(18.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        bars.forEachIndexed { i, (peak, delay) ->
            val frac by transition.animateFloat(
                initialValue = 0.35f,
                targetValue = peak,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 900, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(delay, StartOffsetType.FastForward),
                ),
                label = "bar$i",
            )
            if (i > 0) Spacer(Modifier.width(2.dp))
            Box(
                Modifier
                    .width(3.dp)
                    .fillMaxHeight(frac)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color),
            )
        }
    }
}
