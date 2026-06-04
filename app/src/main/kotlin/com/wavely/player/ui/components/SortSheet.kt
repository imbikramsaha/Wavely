package com.wavely.player.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wavely.domain.model.SortDirection
import com.wavely.domain.model.SortField
import com.wavely.domain.model.SortSpec
import com.wavely.player.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortSheet(
    current: SortSpec,
    fields: List<SortField>,
    onSelect: (SortSpec) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(bottom = Spacing.lg)) {
            Text(
                text = "SORT BY",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            )
            fields.forEach { field ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(selected = current.field == field, onClick = { onSelect(current.copy(field = field)) })
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = current.field == field, onClick = null)
                    Text(
                        text = field.label(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = Spacing.md),
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(current.toggleDirection()) }
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (current.direction == SortDirection.ASC) "Ascending" else "Descending",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Icon(
                    imageVector = if (current.direction == SortDirection.ASC) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                    contentDescription = null,
                )
            }
            TextButton(onClick = onDismiss, modifier = Modifier.padding(horizontal = Spacing.sm)) {
                Text("Done")
            }
        }
    }
}

private fun SortField.label(): String = when (this) {
    SortField.TITLE -> "Title"
    SortField.DATE_ADDED -> "Date added"
    SortField.DURATION -> "Duration"
    SortField.ALBUM -> "Album"
    SortField.ARTIST -> "Artist"
}
