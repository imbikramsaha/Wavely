package com.wavely.player.ui.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wavely.domain.model.Track
import com.wavely.player.R
import com.wavely.player.ui.components.EmptyState
import com.wavely.player.ui.components.WavelyTopBar
import com.wavely.player.ui.components.TrackRow
import com.wavely.player.ui.playlist.AddToPlaylistDialog
import com.wavely.player.ui.theme.Dimens
import com.wavely.player.ui.theme.Spacing
import com.wavely.player.ui.track.TrackActions
import com.wavely.player.ui.track.TrackActionsViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    currentTrackKey: String?,
    onSettings: () -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
    trackActions: TrackActionsViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val favorites by trackActions.favorites.collectAsStateWithLifecycle()
    var addToPlaylistTrack by remember { mutableStateOf<Track?>(null) }
    // Keyboard height (0 when hidden). The results list pads its bottom by the larger of this and
    // the mini-player/nav reserve, so results clear the keyboard with no empty gap above it.
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    // Back clears the query before leaving the screen (design.md §5.2).
    BackHandler(enabled = query.isNotBlank()) { viewModel.onQueryChange("") }

    Column(modifier = modifier.fillMaxSize()) {
        WavelyTopBar(title = stringResource(R.string.nav_search), onSettings = onSettings)
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(Dimens.cornerPill),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
        )

        when {
            query.isBlank() -> EmptyState(
                icon = Icons.Filled.Search,
                title = "Search songs",
                message = "Find anything in your library by title or artist.",
            )
            results.isEmpty -> EmptyState(
                icon = Icons.Filled.Search,
                title = "No results",
                message = "Nothing matches \"$query\".",
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(bottom = maxOf(bottomPadding, imeBottom)),
            ) {
                items(results.tracks.size) { i ->
                    val t = results.tracks[i]
                    val key = t.identity.asKey()
                    TrackRow(
                        track = t,
                        isCurrent = key == currentTrackKey,
                        onClick = { viewModel.playTrack(t) },
                        actions = TrackActions(
                            onPlayNext = { trackActions.playNext(t) },
                            onAddToQueue = { trackActions.addToQueue(t) },
                            onAddToPlaylist = { addToPlaylistTrack = t },
                            isFavorite = favorites.contains(key),
                            onToggleFavorite = { trackActions.toggleFavorite(t) },
                        ),
                    )
                }
            }
        }
    }

    addToPlaylistTrack?.let { track ->
        AddToPlaylistDialog(tracks = listOf(track), onDismiss = { addToPlaylistTrack = null })
    }
}
