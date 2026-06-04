package com.wavely.player.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.wavely.player.ui.detail.DetailScreen
import com.wavely.player.ui.detail.DetailViewModel
import com.wavely.player.ui.library.LibraryScreen
import com.wavely.player.ui.library.LibraryViewModel
import com.wavely.player.ui.player.PlayerViewModel
import com.wavely.player.ui.playlist.PlaylistsScreen
import com.wavely.player.ui.playlist.PlaylistsViewModel
import com.wavely.player.ui.search.SearchScreen
import com.wavely.player.ui.search.SearchViewModel
import com.wavely.player.ui.settings.SettingsScreen

@Composable
fun WavelyNavHost(
    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    bottomPadding: Dp,
) {
    val playbackState by playerViewModel.state.collectAsStateWithLifecycle()
    val currentTrackKey = playbackState.current?.identity?.asKey()

    val openDetail: (DetailType, String) -> Unit = { type, id ->
        navController.navigate(Destination.Detail.route(type, id))
    }
    val openFavorites: () -> Unit = { navController.navigate(Destination.Detail.route(DetailType.FAVORITES, "0")) }
    val openSettings: () -> Unit = { navController.navigate(Destination.Settings.route) }

    NavHost(navController = navController, startDestination = Destination.Library.route) {

        composable(Destination.Library.route) {
            val vm: LibraryViewModel = hiltViewModel()
            LibraryScreen(
                viewModel = vm,
                currentTrackKey = currentTrackKey,
                onSettings = openSettings,
                bottomPadding = bottomPadding,
            )
        }

        composable(Destination.Search.route) {
            val vm: SearchViewModel = hiltViewModel()
            SearchScreen(
                viewModel = vm,
                currentTrackKey = currentTrackKey,
                onSettings = openSettings,
                bottomPadding = bottomPadding,
            )
        }

        composable(Destination.Playlists.route) {
            val vm: PlaylistsViewModel = hiltViewModel()
            PlaylistsScreen(
                viewModel = vm,
                onOpenDetail = openDetail,
                onOpenFavorites = openFavorites,
                onSettings = openSettings,
                bottomPadding = bottomPadding,
            )
        }

        composable(Destination.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Destination.Detail.route,
            arguments = listOf(
                navArgument(Destination.Detail.ARG_TYPE) { type = NavType.StringType },
                navArgument(Destination.Detail.ARG_ID) { type = NavType.StringType },
            ),
        ) {
            val vm: DetailViewModel = hiltViewModel()
            DetailScreen(
                viewModel = vm,
                currentTrackKey = currentTrackKey,
                onBack = { navController.popBackStack() },
                bottomPadding = bottomPadding,
            )
        }
    }
}
