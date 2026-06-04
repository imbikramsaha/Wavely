package com.wavely.player.ui.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.wavely.domain.model.SortField
import com.wavely.domain.model.Track
import com.wavely.player.R
import com.wavely.player.ui.components.EmptyState
import com.wavely.player.ui.components.LoadingState
import com.wavely.player.ui.components.WavelyMark
import com.wavely.player.ui.components.WavelyTopBar
import com.wavely.player.ui.components.SortSheet
import com.wavely.player.ui.components.TrackRow
import com.wavely.player.ui.permission.rememberAudioPermissionState
import com.wavely.player.ui.playlist.AddToPlaylistDialog
import com.wavely.player.ui.theme.Spacing
import com.wavely.player.ui.track.TrackActions
import com.wavely.player.ui.track.TrackActionsViewModel

/**
 * Songs — a single flat list of all tracks (Wavely v2; no album/artist/folder browse).
 * Tap plays in context of the whole list; long-press = multi-select; overflow per row;
 * top bar has Shuffle-all + Sort + Settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    currentTrackKey: String?,
    onSettings: () -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
    trackActions: TrackActionsViewModel = hiltViewModel(),
) {
    val permission = rememberAudioPermissionState(onGranted = { viewModel.rescan() })
    var showSort by remember { mutableStateOf(false) }

    val selected = remember { mutableStateMapOf<String, Track>() }
    val selectionMode = selected.isNotEmpty()
    var addToPlaylistTracks by remember { mutableStateOf<List<Track>?>(null) }

    BackHandler(enabled = selectionMode) { selected.clear() }

    Column(modifier = modifier.fillMaxSize()) {
        if (selectionMode) {
            SelectionBar(
                count = selected.size,
                onClear = { selected.clear() },
                onPlayNext = { viewModel.playNext(selected.values.toList()); selected.clear() },
                onAddToQueue = { viewModel.addToQueue(selected.values.toList()); selected.clear() },
                onAddToPlaylist = { addToPlaylistTracks = selected.values.toList() },
            )
        } else {
            WavelyTopBar(
                title = stringResource(R.string.app_name),
                centered = false,
                onSettings = onSettings,
                titleContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        WavelyMark(size = 24.dp)
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.shuffleAll() }) {
                        Icon(Icons.Filled.Shuffle, contentDescription = stringResource(R.string.action_shuffle_all))
                    }
                    IconButton(onClick = { showSort = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                    }
                },
            )
        }

        if (!permission.granted) {
            EmptyState(
                icon = Icons.Filled.MusicNote,
                message = stringResource(R.string.permission_rationale),
                actionLabel = stringResource(R.string.action_grant_access),
                onAction = permission.request,
            )
            return@Column
        }

        SongList(
            viewModel = viewModel,
            trackActions = trackActions,
            currentTrackKey = currentTrackKey,
            bottomPadding = bottomPadding,
            selected = selected,
            selectionMode = selectionMode,
            onAddToPlaylist = { addToPlaylistTracks = listOf(it) },
        )
    }

    if (showSort) {
        val current = viewModel.tracksSort.collectAsStateWithLifecycle().value
        SortSheet(
            current = current,
            fields = listOf(SortField.TITLE, SortField.DATE_ADDED, SortField.DURATION),
            onSelect = { viewModel.setSort(it) },
            onDismiss = { showSort = false },
        )
    }

    addToPlaylistTracks?.let { tracks ->
        AddToPlaylistDialog(
            tracks = tracks,
            onDismiss = {
                addToPlaylistTracks = null
                selected.clear()
            },
        )
    }
}

@Composable
private fun SongList(
    viewModel: LibraryViewModel,
    trackActions: TrackActionsViewModel,
    currentTrackKey: String?,
    bottomPadding: Dp,
    selected: SnapshotStateMap<String, Track>,
    selectionMode: Boolean,
    onAddToPlaylist: (Track) -> Unit,
) {
    val items = viewModel.tracks.collectAsLazyPagingItems()
    val favorites by trackActions.favorites.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()

    if (items.itemCount == 0) {
        // Distinguish "still scanning" (spinner) from "scanned, nothing here" (empty state)
        // so first launch doesn't flash "No music on this device".
        if (loading) LoadingState()
        else EmptyState(icon = Icons.Filled.MusicNote, message = stringResource(R.string.empty_no_music))
        return
    }
    LazyColumn(contentPadding = PaddingValues(bottom = bottomPadding)) {
        items(count = items.itemCount, key = items.itemKey { it.identity.asKey() }) { index ->
            val track = items[index] ?: return@items
            val key = track.identity.asKey()
            TrackRow(
                track = track,
                isCurrent = key == currentTrackKey,
                onClick = {
                    if (selectionMode) toggleSelection(selected, track)
                    else viewModel.onTrackSelected(track)
                },
                onLongClick = { toggleSelection(selected, track) },
                selectionMode = selectionMode,
                selected = selected.containsKey(key),
                actions = if (selectionMode) null else TrackActions(
                    onPlayNext = { viewModel.playNext(listOf(track)) },
                    onAddToQueue = { viewModel.addToQueue(listOf(track)) },
                    onAddToPlaylist = { onAddToPlaylist(track) },
                    isFavorite = favorites.contains(key),
                    onToggleFavorite = { trackActions.toggleFavorite(track) },
                ),
            )
        }
    }
}

private fun toggleSelection(selected: SnapshotStateMap<String, Track>, track: Track) {
    val key = track.identity.asKey()
    if (selected.containsKey(key)) selected.remove(key) else selected[key] = track
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionBar(
    count: Int,
    onClear: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
) {
    TopAppBar(
        title = { Text("$count selected", style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = onClear) { Icon(Icons.Filled.Close, contentDescription = "Clear selection") }
        },
        actions = {
            IconButton(onClick = onPlayNext) { Icon(Icons.Filled.SkipNext, contentDescription = "Play next") }
            IconButton(onClick = onAddToQueue) { Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = "Add to queue") }
            IconButton(onClick = onAddToPlaylist) { Icon(Icons.Filled.Add, contentDescription = "Add to playlist") }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}
