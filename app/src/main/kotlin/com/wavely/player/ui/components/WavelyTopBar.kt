package com.wavely.player.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wavely.player.R

/**
 * App bar with two layouts: [centered] = true gives the centered screen title (Search,
 * Playlists); [centered] = false gives a left-aligned bar used for the Songs brand row.
 * Provide [titleContent] to render something other than the plain title (e.g. the brand
 * mark + wordmark). Trailing slot is the Settings gear (§1.2).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WavelyTopBar(
    title: String,
    modifier: Modifier = Modifier,
    centered: Boolean = true,
    onSettings: (() -> Unit)? = null,
    titleContent: (@Composable () -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    val titleSlot: @Composable () -> Unit = titleContent ?: {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
    }
    val trailing: @Composable () -> Unit = {
        actions()
        if (onSettings != null) {
            IconButton(onClick = onSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.cd_settings),
                )
            }
        }
    }
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
    )
    if (centered) {
        CenterAlignedTopAppBar(
            modifier = modifier,
            title = titleSlot,
            actions = { trailing() },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurface,
            ),
        )
    } else {
        TopAppBar(
            modifier = modifier,
            title = titleSlot,
            actions = { trailing() },
            colors = colors,
        )
    }
}
