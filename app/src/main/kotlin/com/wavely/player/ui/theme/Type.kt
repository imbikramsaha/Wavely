package com.wavely.player.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.wavely.player.R

/**
 * WAVELY v2 — Hanken Grotesk (bundled variable font, OFL) mapped to the M3 type scale.
 * One TTF exposes the weights the design uses (400/500/600/700/800) via font variation
 * settings; the weight axis is honoured on API 26+ (the app's minSdk).
 */
@OptIn(ExperimentalTextApi::class)
private fun hk(weight: Int) = Font(
    R.font.hanken_grotesk,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val HankenGrotesk = FontFamily(hk(400), hk(500), hk(600), hk(700), hk(800))

private val HK = HankenGrotesk

val WavelyTypography = Typography(
    // Large screen / Now Playing / dialog titles (wv-title / NP title / h3): 21 / 700 / -.02em
    titleLarge = TextStyle(fontFamily = HK, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 27.sp, letterSpacing = (-0.3).sp),
    // Track row title (wv-tt): 15.5 / 600 / -.01em
    titleMedium = TextStyle(fontFamily = HK, fontWeight = FontWeight.SemiBold, fontSize = 15.5.sp, lineHeight = 20.sp, letterSpacing = (-0.1).sp),
    // Now Playing artist line + tab labels (wv subtitle): 14 / 500
    titleSmall = TextStyle(fontFamily = HK, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 18.sp),
    // Body / dialog options (wv-opt): 16 / 500
    bodyLarge = TextStyle(fontFamily = HK, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp),
    // List secondary line (wv-sub): 12.5 / 500
    bodyMedium = TextStyle(fontFamily = HK, fontWeight = FontWeight.Medium, fontSize = 12.5.sp, lineHeight = 16.sp),
    // Timestamps / captions (seekbar): 12 / 600
    bodySmall = TextStyle(fontFamily = HK, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
    // Buttons (wv-btn): 15 / 700
    labelLarge = TextStyle(fontFamily = HK, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 18.sp),
    // Sheet titles "SORT BY" (uppercase applied at usage): 13 / 700 / +.08em
    labelMedium = TextStyle(fontFamily = HK, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, lineHeight = 16.sp, letterSpacing = 1.0.sp),
    // Bottom-nav + utility labels: 11.5 / 600
    labelSmall = TextStyle(fontFamily = HK, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp, lineHeight = 14.sp, letterSpacing = 0.1.sp),
)
