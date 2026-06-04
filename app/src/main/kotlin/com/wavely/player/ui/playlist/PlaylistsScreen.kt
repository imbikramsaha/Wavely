package com.wavely.player.ui.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wavely.domain.model.Playlist
import com.wavely.player.R
import com.wavely.player.navigation.DetailType
import com.wavely.player.ui.components.WavelyTopBar
import com.wavely.player.ui.components.TextInputDialog
import com.wavely.player.ui.theme.Dimens
import com.wavely.player.ui.theme.Spacing
import com.wavely.player.ui.theme.brandGradient
import com.wavely.player.ui.theme.wvFallbackBrush
import com.wavely.player.ui.util.pluralTracks

@Composable
fun PlaylistsScreen(
    viewModel: PlaylistsViewModel,
    onOpenDetail: (DetailType, String) -> Unit,
    onOpenFavorites: () -> Unit,
    onSettings: () -> Unit,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val favoritesCount by viewModel.favoritesCount.collectAsStateWithLifecycle()

    var creating by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf<Playlist?>(null) }
    var deleting by remember { mutableStateOf<Playlist?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        WavelyTopBar(
            title = stringResource(R.string.nav_playlists),
            onSettings = onSettings,
            actions = {
                IconButton(onClick = { creating = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "New playlist")
                }
            },
        )

        LazyColumn(contentPadding = PaddingValues(bottom = bottomPadding)) {
            // Pinned Favorites entry.
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenFavorites)
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlaylistTile(brush = brandGradient, icon = Icons.Filled.Favorite)
                    Column(modifier = Modifier.weight(1f).padding(start = Spacing.md)) {
                        Text(stringResource(R.string.title_favorites), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(pluralTracks(favoritesCount), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }

            items(playlists.size) { i ->
                val pl = playlists[i]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDetail(DetailType.PLAYLIST, pl.id.toString()) }
                        .padding(start = Spacing.md, top = Spacing.sm, bottom = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlaylistTile(brush = wvFallbackBrush(pl.name), icon = Icons.AutoMirrored.Filled.QueueMusic)
                    Column(modifier = Modifier.weight(1f).padding(start = Spacing.md)) {
                        Text(pl.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(pluralTracks(pl.trackCount), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    PlaylistRowMenu(onRename = { renaming = pl }, onDelete = { deleting = pl })
                }
            }

            if (playlists.isEmpty()) {
                item {
                    Text(
                        "No playlists yet. Tap + to create one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(Spacing.md),
                    )
                }
            }
        }
    }

    if (creating) {
        TextInputDialog(
            title = "New playlist",
            confirmLabel = "Create",
            onConfirm = { viewModel.createPlaylist(it); creating = false },
            onDismiss = { creating = false },
        )
    }
    renaming?.let { pl ->
        TextInputDialog(
            title = "Rename playlist",
            initialValue = pl.name,
            onConfirm = { viewModel.renamePlaylist(pl, it); renaming = null },
            onDismiss = { renaming = null },
        )
    }
    deleting?.let { pl ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete playlist?") },
            text = { Text("\"${pl.name}\" will be permanently deleted. Your audio files are not affected.") },
            confirmButton = { TextButton(onClick = { viewModel.deletePlaylist(pl); deleting = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } },
        )
    }
}

/** Rounded gradient artwork tile for a playlist row (brand gradient for Favorites). */
@Composable
private fun PlaylistTile(brush: androidx.compose.ui.graphics.Brush, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .size(Dimens.thumbnail)
            .clip(RoundedCornerShape(Dimens.cornerTile))
            .background(brush),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun PlaylistRowMenu(onRename: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.cd_overflow))
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(text = { Text("Rename") }, onClick = { expanded = false; onRename() })
        DropdownMenuItem(text = { Text("Delete") }, onClick = { expanded = false; onDelete() })
    }
}
