package com.wavely.player.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

/* ============================================================================
 * WAVELY v2 — brand gradient + generated fallback-art math (port of
 * v2 design/wavely/art.jsx). The gradient is fixed across themes & dynamic color.
 * ========================================================================== */

private val GradientStops = arrayOf(0f to WavelyCyan, 0.52f to WavelyBlue, 1f to WavelyIndigo)

/** Horizontal cyan→indigo brand gradient (logo mark, play button, mini-player progress). */
val brandGradient: Brush = Brush.horizontalGradient(*GradientStops)

/** Vertical variant for tall surfaces. */
val brandGradientVertical: Brush = Brush.verticalGradient(*GradientStops)

/**
 * Stable title → hue [0,360). EXACT port of art.jsx `wvHue` so a track always gets the
 * same fallback tile across the app (a fidelity contract — do not change the formula).
 */
fun wvHue(title: String): Int {
    var h = 0
    for (c in title) h = (h * 31 + c.code) % 360
    return h
}

/** First letter/digit of the title (skips the "AUD-" recorder prefix punctuation), upper-cased. */
fun wvMonogram(title: String): String {
    val m = title.trimStart { !it.isLetterOrDigit() }
    return m.firstOrNull()?.uppercaseChar()?.toString() ?: "♪"
}

/** HSL → Color (Compose has no HSL constructor). h in [0,360), s/l in [0,1]. */
fun hsl(h: Float, s: Float, l: Float): Color {
    val c = (1f - abs(2f * l - 1f)) * s
    val hp = (h % 360f) / 60f
    val x = c * (1f - abs(hp % 2f - 1f))
    val (r, g, b) = when {
        hp < 1f -> Triple(c, x, 0f)
        hp < 2f -> Triple(x, c, 0f)
        hp < 3f -> Triple(0f, c, x)
        hp < 4f -> Triple(0f, x, c)
        hp < 5f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    val m = l - c / 2f
    // coerce defensively — Color(Float,Float,Float) requires components in [0,1].
    return Color((r + m).coerceIn(0f, 1f), (g + m).coerceIn(0f, 1f), (b + m).coerceIn(0f, 1f))
}

/** The two stops of a track's generated tile: hsl(h,55,50) → hsl(h+42,60,38). */
fun wvArtColors(title: String): Pair<Color, Color> {
    val h = wvHue(title).toFloat()
    return hsl(h, 0.55f, 0.50f) to hsl((h + 42f) % 360f, 0.60f, 0.38f)
}

/** 145°-ish diagonal gradient fallback tile for an art-less track. */
fun wvFallbackBrush(title: String): Brush {
    val (a, b) = wvArtColors(title)
    return Brush.linearGradient(listOf(a, b))
}
