package com.wavely.player.ui.track

/**
 * Callbacks for a track row's overflow menu (design.md §9). Null entries are hidden,
 * letting different screens expose only the relevant actions.
 */
data class TrackActions(
    val onPlayNext: () -> Unit,
    val onAddToQueue: () -> Unit,
    val onAddToPlaylist: () -> Unit,
    val isFavorite: Boolean,
    val onToggleFavorite: () -> Unit,
    val onGoToAlbum: (() -> Unit)? = null,
    val onGoToArtist: (() -> Unit)? = null,
    val onRemove: (() -> Unit)? = null,
    val removeLabel: String = "Remove",
)
