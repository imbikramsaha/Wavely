package com.wavely.player.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wavely.domain.model.Track
import com.wavely.player.R
import com.wavely.player.ui.theme.Dimens
import com.wavely.player.ui.theme.Spacing
import com.wavely.player.ui.track.TrackActions
import com.wavely.player.ui.util.formatDuration
import com.wavely.player.ui.util.secondaryLine

/**
 * Track list row — design.md §2.3. 64dp tall, 48dp thumbnail. The currently-playing
 * row shows its title in accent + an equalizer glyph in place of the thumbnail.
 *
 * Provide [actions] for a full overflow menu, or [onOverflow] for a plain trailing-tap.
 * Pass [selected]/[selectionMode] to render a multi-select check (design.md §9 long-press).
 */
@Composable
fun TrackRow(
    track: Track,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: TrackActions? = null,
    onOverflow: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    selectionMode: Boolean = false,
    selected: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.sm, vertical = 2.dp)
            .clip(RoundedCornerShape(Dimens.cornerCard))
            .background(if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
            .combinedClickableCompat(onClick = onClick, onLongClick = onLongClick)
            .heightIn(min = Dimens.listItemHeight)
            .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(Dimens.thumbnail), contentAlignment = Alignment.Center) {
            when {
                selectionMode -> SelectionCheck(selected)
                isCurrent -> PlayingArt(title = track.title, modifier = Modifier.fillMaxSize())
                else -> AlbumArt(uri = track.albumArtUri, seed = track.title, modifier = Modifier.fillMaxSize())
            }
        }

        Column(modifier = Modifier.weight(1f).padding(start = Spacing.md)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = secondaryLine(track.artist, formatDuration(track.durationMs)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (!selectionMode) {
            when {
                actions != null -> TrackOverflowMenu(actions)
                onOverflow != null -> IconButton(onClick = onOverflow) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.cd_overflow),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.combinedClickableCompat(onClick: () -> Unit, onLongClick: (() -> Unit)?): Modifier =
    this.combinedClickable(onClick = onClick, onLongClick = onLongClick)

/** Circular multi-select indicator — outline when unchecked, accent-filled with a check when on. */
@Composable
private fun SelectionCheck(selected: Boolean) {
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .then(
                if (selected) Modifier.background(accent)
                else Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(15.dp),
            )
        }
    }
}

@Composable
private fun TrackOverflowMenu(actions: TrackActions) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.cd_overflow),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Play next") },
                leadingIcon = { Icon(Icons.Filled.SkipNext, null) },
                onClick = { expanded = false; actions.onPlayNext() },
            )
            DropdownMenuItem(
                text = { Text("Add to queue") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.QueueMusic, null) },
                onClick = { expanded = false; actions.onAddToQueue() },
            )
            DropdownMenuItem(
                text = { Text("Add to playlist") },
                leadingIcon = { Icon(Icons.Filled.Add, null) },
                onClick = { expanded = false; actions.onAddToPlaylist() },
            )
            DropdownMenuItem(
                text = { Text(if (actions.isFavorite) "Remove from favorites" else "Add to favorites") },
                leadingIcon = {
                    Icon(if (actions.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, null)
                },
                onClick = { expanded = false; actions.onToggleFavorite() },
            )
            actions.onGoToAlbum?.let { go ->
                DropdownMenuItem(
                    text = { Text("Go to album") },
                    leadingIcon = { Icon(Icons.Filled.Album, null) },
                    onClick = { expanded = false; go() },
                )
            }
            actions.onGoToArtist?.let { go ->
                DropdownMenuItem(
                    text = { Text("Go to artist") },
                    leadingIcon = { Icon(Icons.Filled.Person, null) },
                    onClick = { expanded = false; go() },
                )
            }
            actions.onRemove?.let { remove ->
                DropdownMenuItem(
                    text = { Text(actions.removeLabel) },
                    leadingIcon = { Icon(Icons.Filled.Delete, null) },
                    onClick = { expanded = false; remove() },
                )
            }
        }
    }
}
