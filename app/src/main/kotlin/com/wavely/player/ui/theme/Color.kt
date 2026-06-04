package com.wavely.player.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/* ============================================================================
 * WAVELY v2 — "calm minimal" palette (see v2 design/wavely/tokens.css).
 * Deep neutrals + a serene cyan→indigo brand gradient. Dark is primary.
 * ========================================================================== */

// ---- Brand gradient (logo: cyan → blue → indigo). FIXED across themes & dynamic color. ----
val WavelyCyan = Color(0xFF2FD3E6)
val WavelyBlue = Color(0xFF5B8DF4)
val WavelyIndigo = Color(0xFF8E7BFF)
val WavelyGradientStops = listOf(WavelyCyan, WavelyBlue, WavelyIndigo)

// ---- DARK (primary) ----
val DarkBg = Color(0xFF0A0C10)
val DarkBg2 = Color(0xFF0E1116)
val DarkSurface = Color(0xFF12161C)
val DarkSurface2 = Color(0xFF171C23)
val DarkSurface3 = Color(0xFF1E242D)
val DarkHover = Color(0xFF232A34)
val DarkLine = Color(0x12FFFFFF)   // white .07
val DarkLine2 = Color(0x1FFFFFFF)  // white .12
val DarkText = Color(0xFFEAEEF3)
val DarkText2 = Color(0xFF99A3B0)
val DarkText3 = Color(0xFF636E7B)
val DarkAccent = Color(0xFF6CA8FF)
val DarkAccentDim = Color(0xFF274160)
val DarkOnAccent = Color(0xFF061320)
val DarkScrim = Color(0xA8040609)  // rgba(4,6,9,.66)
val DarkEq = Color(0xFF7FE9F0)
val DarkError = Color(0xFFF28B82)

// ---- LIGHT ----
val LightBg = Color(0xFFEEF1F6)
val LightBg2 = Color(0xFFF4F6FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurface2 = Color(0xFFFFFFFF)
val LightSurface3 = Color(0xFFEAEEF4)
val LightHover = Color(0xFFE3E9F1)
val LightLine = Color(0x17101620)  // ink .09
val LightLine2 = Color(0x29101620) // ink .16
val LightText = Color(0xFF121722)
val LightText2 = Color(0xFF59636F)
val LightText3 = Color(0xFF8A94A1)
val LightAccent = Color(0xFF2F6BE0)
val LightAccentDim = Color(0xFFCFE0FB)
val LightOnAccent = Color(0xFFFFFFFF)
val LightScrim = Color(0x52141A26)  // rgba(20,26,38,.32)
val LightEq = Color(0xFF2FB5C8)
val LightError = Color(0xFFD93025)

/**
 * Brand/UX tokens that don't map cleanly onto a single Material 3 [androidx.compose.material3.ColorScheme]
 * slot (3 surface elevations, hairline dividers, a tertiary text tint, the equalizer tint, and the brand
 * gradient). Carried alongside the M3 scheme via [LocalWavelyColors]. The gradient stays fixed even under
 * dynamic color, per the design handoff.
 */
@Immutable
data class WavelyExtendedColors(
    val bg2: Color,
    val surface2: Color,
    val surface3: Color,
    val hover: Color,
    val line: Color,
    val line2: Color,
    val text3: Color,
    val accentDim: Color,
    val onAccent: Color,
    val eq: Color,
    val gradient: List<Color>,
    val isDark: Boolean,
)

val DarkWavelyExtended = WavelyExtendedColors(
    bg2 = DarkBg2, surface2 = DarkSurface2, surface3 = DarkSurface3, hover = DarkHover,
    line = DarkLine, line2 = DarkLine2, text3 = DarkText3, accentDim = DarkAccentDim,
    onAccent = DarkOnAccent, eq = DarkEq, gradient = WavelyGradientStops, isDark = true,
)

val LightWavelyExtended = WavelyExtendedColors(
    bg2 = LightBg2, surface2 = LightSurface2, surface3 = LightSurface3, hover = LightHover,
    line = LightLine, line2 = LightLine2, text3 = LightText3, accentDim = LightAccentDim,
    onAccent = LightOnAccent, eq = LightEq, gradient = WavelyGradientStops, isDark = false,
)
