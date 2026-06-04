package com.wavely.player.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wavely.domain.model.ThemeMode

/** Access the Wavely brand/extended tokens that sit alongside the M3 [ColorScheme]. */
val LocalWavelyColors = staticCompositionLocalOf { DarkWavelyExtended }

private val DarkColors = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkOnAccent,
    primaryContainer = DarkAccentDim,
    onPrimaryContainer = DarkAccent,
    secondary = DarkAccent,
    onSecondary = DarkOnAccent,
    tertiary = WavelyIndigo,
    background = DarkBg,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurface3,
    onSurfaceVariant = DarkText2,
    surfaceContainerLowest = DarkBg,
    surfaceContainerLow = DarkBg2,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurface2,
    surfaceContainerHighest = DarkSurface3,
    surfaceTint = DarkAccent,
    inverseSurface = DarkSurface3,
    inverseOnSurface = DarkText,
    outline = DarkLine2,
    outlineVariant = DarkLine,
    scrim = DarkScrim,
    error = DarkError,
    onError = DarkOnAccent,
)

private val LightColors = lightColorScheme(
    primary = LightAccent,
    onPrimary = LightOnAccent,
    primaryContainer = LightAccentDim,
    onPrimaryContainer = LightAccent,
    secondary = LightAccent,
    onSecondary = LightOnAccent,
    tertiary = WavelyIndigo,
    background = LightBg,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurface3,
    onSurfaceVariant = LightText2,
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = LightBg2,
    surfaceContainer = LightBg,
    surfaceContainerHigh = LightSurface3,
    surfaceContainerHighest = LightSurface3,
    surfaceTint = LightAccent,
    inverseSurface = LightText,
    inverseOnSurface = LightBg,
    outline = LightLine2,
    outlineVariant = LightLine,
    scrim = LightScrim,
    error = LightError,
    onError = LightOnAccent,
)

private val WavelyShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(Dimens.cornerTile),
    medium = RoundedCornerShape(Dimens.cornerCard),
    large = RoundedCornerShape(Dimens.cornerDialog),
    extraLarge = RoundedCornerShape(Dimens.cornerSheet),
)

/** Brand tokens derived from a dynamic [ColorScheme] (Material You). Gradient + eq stay fixed. */
private fun dynamicExtended(scheme: ColorScheme, dark: Boolean) = WavelyExtendedColors(
    bg2 = scheme.surfaceContainerLow,
    surface2 = scheme.surfaceContainerHigh,
    surface3 = scheme.surfaceContainerHighest,
    hover = scheme.surfaceContainerHighest,
    line = scheme.outlineVariant,
    line2 = scheme.outline,
    text3 = scheme.onSurfaceVariant.copy(alpha = 0.66f),
    accentDim = scheme.primaryContainer,
    onAccent = scheme.onPrimary,
    eq = if (dark) DarkEq else LightEq,
    gradient = WavelyGradientStops,
    isDark = dark,
)

@Composable
fun WavelyTheme(
    themeMode: ThemeMode,
    dynamicColor: Boolean,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val context = LocalContext.current
    val useDynamic = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        useDynamic -> if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }
    val extended = when {
        useDynamic -> dynamicExtended(colorScheme, dark)
        dark -> DarkWavelyExtended
        else -> LightWavelyExtended
    }

    CompositionLocalProvider(LocalWavelyColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = WavelyTypography,
            shapes = WavelyShapes,
            content = content,
        )
    }
}
