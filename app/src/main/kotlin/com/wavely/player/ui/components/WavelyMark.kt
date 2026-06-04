package com.wavely.player.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wavely.player.ui.theme.brandGradient

/**
 * Wavely brand mark — five rounded bars (small · tall · tallest · tall · small) painted with
 * the cyan→indigo brand gradient. Vector port of v2 design/exports `wavely-mark.svg`
 * (viewBox 136×130). All bars are filled as ONE path so the gradient sweeps across the whole
 * mark (drawing per-bar would map the full gradient onto each 16px bar). Pass [mono] for a flat colour.
 */
@Composable
fun WavelyMark(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    mono: Color? = null,
) {
    val brush: Brush = mono?.let { SolidColor(it) } ?: brandGradient
    // x, y, height for each bar (width 16, corner 8) in the 136×130 viewBox
    val bars = listOf(
        floatArrayOf(0f, 45f, 40f),
        floatArrayOf(30f, 29f, 72f),
        floatArrayOf(60f, 10f, 110f),
        floatArrayOf(90f, 29f, 72f),
        floatArrayOf(120f, 45f, 40f),
    )
    Canvas(modifier = modifier.size(width = size, height = size * (130f / 136f))) {
        val px = this.size.width / 136f
        val r = CornerRadius(8f * px, 8f * px)
        val path = Path().apply {
            bars.forEach { b ->
                addRoundRect(
                    RoundRect(
                        left = b[0] * px,
                        top = b[1] * px,
                        right = (b[0] + 16f) * px,
                        bottom = (b[1] + b[2]) * px,
                        cornerRadius = r,
                    ),
                )
            }
        }
        drawPath(path, brush)
    }
}
