package com.wavely.player.ui.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wavely.player.ui.theme.Spacing

/** Sleep timer presets (design.md §2.2 utility row). Returns an absolute end time, or null to cancel. */
@Composable
fun SleepTimerDialog(
    active: Boolean,
    nowMs: () -> Long,
    onPick: (Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    val presetsMin = listOf(15, 30, 45, 60)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sleep timer") },
        text = {
            Column {
                presetsMin.forEach { min ->
                    Text(
                        text = "$min minutes",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = false, onClick = { onPick(nowMs() + min * 60_000L) })
                            .padding(vertical = Spacing.sm),
                    )
                }
            }
        },
        confirmButton = {
            if (active) {
                TextButton(onClick = { onPick(null) }) { Text("Turn off") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
