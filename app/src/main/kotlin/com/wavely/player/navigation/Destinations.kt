package com.wavely.player.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.wavely.player.R

/** Top-level navigation destinations. Only [topLevel] entries appear in the bottom nav (exactly 3). */
sealed class Destination(val route: String) {

    // Songs (the flat song list) — route kept as "library" to avoid DI/route churn.
    data object Library : Destination("library")
    data object Search : Destination("search")
    data object Playlists : Destination("playlists")
    data object Settings : Destination("settings")

    // Detail — pushed; a user playlist or the Favorites list.
    data object Detail : Destination("detail/{type}/{id}") {
        fun route(type: DetailType, id: String): String =
            "detail/${type.name}/${android.net.Uri.encode(id)}"
        const val ARG_TYPE = "type"
        const val ARG_ID = "id"
    }
}

enum class DetailType { PLAYLIST, FAVORITES }

data class TopLevelDestination(
    val destination: Destination,
    val icon: ImageVector,
    val labelRes: Int,
)

val topLevelDestinations = listOf(
    TopLevelDestination(Destination.Library, Icons.Filled.MusicNote, R.string.nav_songs),
    TopLevelDestination(Destination.Search, Icons.Filled.Search, R.string.nav_search),
    TopLevelDestination(Destination.Playlists, Icons.AutoMirrored.Filled.PlaylistPlay, R.string.nav_playlists),
)
