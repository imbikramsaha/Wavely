package com.wavely.player.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Width-based window size class — design.md §8.2 (600dp / 840dp breakpoints). */
enum class WindowWidth { COMPACT, MEDIUM, EXPANDED }

fun widthClassOf(maxWidth: Dp): WindowWidth = when {
    maxWidth < 600.dp -> WindowWidth.COMPACT
    maxWidth < 840.dp -> WindowWidth.MEDIUM
    else -> WindowWidth.EXPANDED
}

/** Album grid columns per width class (§4.2 / §8.2). */
fun WindowWidth.albumColumns(): Int = when (this) {
    WindowWidth.COMPACT -> 2
    WindowWidth.MEDIUM -> 3
    WindowWidth.EXPANDED -> 4
}
