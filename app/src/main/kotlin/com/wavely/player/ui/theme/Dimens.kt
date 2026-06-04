package com.wavely.player.ui.theme

import androidx.compose.ui.unit.dp

/** 4dp-base spacing scale — design.md §4.1 / v2 tokens.css (--wv-1..8). */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp // default edge margin
    val lg = 24.dp // section separation
    val xl = 32.dp // Now Playing rhythm
}

object Dimens {
    val miniPlayerHeight = 64.dp
    val listItemHeight = 64.dp
    val thumbnail = 52.dp        // v2 row artwork (was 48)
    val miniThumbnail = 44.dp    // v2 mini-player artwork
    val bottomNavHeight = 56.dp
    val appBarHeight = 56.dp
    val touchTarget = 48.dp

    // v2 corner radii (tokens.css)
    val cornerTile = 14.dp       // album-art tiles
    val cornerCard = 18.dp       // rows, mini-player, cards
    val cornerSheet = 28.dp      // bottom sheets
    val cornerDialog = 26.dp     // dialogs
    val cornerPill = 999.dp      // fully rounded

    // legacy aliases (kept so existing call sites compile)
    val cornerSmall = 4.dp
    val cornerMedium = 8.dp
}
