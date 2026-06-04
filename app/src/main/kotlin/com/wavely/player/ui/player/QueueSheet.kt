package com.wavely.player.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wavely.domain.model.PlaybackUiState
import com.wavely.domain.model.Track
import com.wavely.player.ui.components.AlbumArt
import com.wavely.player.ui.components.PlayingArt
import com.wavely.player.ui.components.reorder.DraggableItem
import com.wavely.player.ui.components.reorder.dragContainer
import com.wavely.player.ui.components.reorder.rememberDragDropState
import com.wavely.player.ui.snackbar.LocalSnackbarController
import com.wavely.player.ui.theme.Dimens
import com.wavely.player.ui.theme.Spacing
import com.wavely.player.ui.util.formatDuration
import com.wavely.player.ui.util.secondaryLine

/**
 * The current play queue, shown as a modal bottom sheet from Now Playing
 * (the queue is no longer a top-level tab — Wavely v2). Long-press drag-reorder,
 * swipe-to-remove with Undo. Reuses the reorder primitives from ReorderableList.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSheet(
    state: PlaybackUiState,
    onSkipTo: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
    onUndoRemove: (track: Track, anchorKey: String?, fallbackIndex: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbar = LocalSnackbarController.current
    val listState = rememberLazyListState()
    val dragDropState = rememberDragDropState(listState) { from, to -> onMove(from, to) }
    val queue = state.queue

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Text(
            text = "Up next",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
        )
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = Spacing.xl),
            modifier = Modifier.dragContainer(dragDropState),
        ) {
            items(queue.size) { index ->
                DraggableItem(dragDropState, index) { isDragging ->
                    val track = queue.getOrNull(index)
                    key(track?.identity?.asKey() ?: index, index) {
                        if (track != null) {
                            // Anchor the undo to the track that precedes this one, so re-insertion
                            // lands in the right spot even if the queue is edited before Undo.
                            val anchorKey = queue.getOrNull(index - 1)?.identity?.asKey()
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value != SwipeToDismissBoxValue.Settled) {
                                        onRemove(index)
                                        snackbar.showUndo("Removed ${track.title}") {
                                            onUndoRemove(track, anchorKey, index)
                                        }
                                        true
                                    } else {
                                        false
                                    }
                                },
                            )
                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = { SwipeRemoveBackground() },
                            ) {
                                QueueRow(
                                    track = track,
                                    isCurrent = index == state.queueIndex,
                                    isDragging = isDragging,
                                    onClick = { onSkipTo(index) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeRemoveBackground() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
    }
}

@Composable
private fun QueueRow(track: Track, isCurrent: Boolean, isDragging: Boolean, onClick: () -> Unit) {
    val bg = if (isDragging) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md)
            .height(Dimens.listItemHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isCurrent) {
            PlayingArt(title = track.title, modifier = Modifier.size(Dimens.thumbnail))
        } else {
            AlbumArt(uri = track.albumArtUri, seed = track.title, modifier = Modifier.size(Dimens.thumbnail))
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
        Icon(
            Icons.Filled.DragHandle,
            contentDescription = "Reorder",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
