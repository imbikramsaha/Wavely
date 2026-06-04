package com.wavely.player.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wavely.domain.model.Track
import com.wavely.player.ui.components.EmptyState
import com.wavely.player.ui.components.LoadingState
import com.wavely.player.ui.components.TextInputDialog
import com.wavely.player.ui.components.TrackRow
import com.wavely.player.ui.components.reorder.DraggableItem
import com.wavely.player.ui.components.reorder.dragContainer
import com.wavely.player.ui.components.reorder.rememberDragDropState
import com.wavely.player.ui.playlist.AddToPlaylistDialog
import com.wavely.player.ui.snackbar.LocalSnackbarController
import com.wavely.player.ui.track.TrackActions
import com.wavely.player.ui.track.TrackActionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    currentTrackKey: String?,
    onBack: () -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
    trackActions: TrackActionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val favorites by trackActions.favorites.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbarController.current

    var renaming by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var addToPlaylistTrack by remember { mutableStateOf<Track?>(null) }

    val listState = rememberLazyListState()
    val dragDropState = rememberDragDropState(listState) { from, to -> viewModel.moveTrack(from, to) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(state.title, style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            },
            actions = {
                IconButton(onClick = viewModel::shufflePlay) {
                    Icon(Icons.Filled.Shuffle, contentDescription = "Shuffle play")
                }
                if (state.isPlaylist) {
                    var menu by remember { mutableStateOf(false) }
                    IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "More") }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            leadingIcon = { Icon(Icons.Filled.Edit, null) },
                            onClick = { menu = false; renaming = true },
                        )
                        DropdownMenuItem(
                            text = { Text("Delete playlist") },
                            leadingIcon = { Icon(Icons.Filled.Delete, null) },
                            onClick = { menu = false; confirmDelete = true },
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
            ),
        )

        val tracks = state.tracks
        when {
            state.loading -> LoadingState()
            tracks.isEmpty() -> EmptyState(
                icon = if (state.isFavorites) Icons.Filled.FavoriteBorder else Icons.Filled.MusicNote,
                message = if (state.isFavorites)
                    "No favorites yet. Tap the heart on any song to add it here."
                else
                    "This playlist is empty. Add songs from the song list or Search.",
            )
            else -> LazyColumn(
                state = listState,
                contentPadding = PaddingValues(bottom = bottomPadding),
                modifier = if (state.isPlaylist) Modifier.dragContainer(dragDropState) else Modifier,
            ) {
            items(tracks.size) { index ->
                val track = tracks[index]
                val key = track.identity.asKey()
                val actions = TrackActions(
                    onPlayNext = { viewModel.playNext(track) },
                    onAddToQueue = { viewModel.addToQueue(track) },
                    onAddToPlaylist = { addToPlaylistTrack = track },
                    isFavorite = favorites.contains(key),
                    onToggleFavorite = { trackActions.toggleFavorite(track) },
                    onRemove = when {
                        state.isPlaylist -> {
                            {
                                viewModel.removeTrack(track)
                                snackbar.showUndo("Removed ${track.title}") { viewModel.restoreTrack(index, track) }
                            }
                        }
                        state.isFavorites -> {
                            {
                                viewModel.removeFavorite(track)
                                snackbar.showUndo("Removed from favorites") { viewModel.restoreFavorite(track) }
                            }
                        }
                        else -> null
                    },
                    removeLabel = if (state.isFavorites) "Remove from favorites" else "Remove from playlist",
                )
                if (state.isPlaylist) {
                    DraggableItem(dragDropState, index) { _ ->
                        TrackRow(
                            track = track,
                            isCurrent = key == currentTrackKey,
                            onClick = { viewModel.play(index) },
                            actions = actions,
                        )
                    }
                } else {
                    TrackRow(
                        track = track,
                        isCurrent = key == currentTrackKey,
                        onClick = { viewModel.play(index) },
                        actions = actions,
                    )
                }
            }
            }
        }
    }

    if (renaming) {
        TextInputDialog(
            title = "Rename playlist",
            initialValue = state.title,
            onConfirm = { viewModel.rename(it); renaming = false },
            onDismiss = { renaming = false },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete playlist?") },
            text = { Text("\"${state.title}\" will be permanently deleted. Your audio files are not affected.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.deletePlaylist(onDone = onBack)
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }

    addToPlaylistTrack?.let { track ->
        AddToPlaylistDialog(tracks = listOf(track), onDismiss = { addToPlaylistTrack = null })
    }
}
