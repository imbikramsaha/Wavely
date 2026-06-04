package com.wavely.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.wavely.player.ui.theme.Dimens

/**
 * Album artwork via Coil. When there's no embedded art (the common case for this library),
 * the fallback is the v2 generated gradient tile keyed on [seed] (the track title) instead
 * of a flat placeholder — so every track gets a stable, distinct tile. Square unless a
 * parent constrains it.
 */
@Composable
fun AlbumArt(
    uri: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = Dimens.cornerTile,
    seed: String? = null,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(modifier = modifier.clip(shape), contentAlignment = Alignment.Center) {
        Fallback(seed, cornerRadius)
        if (uri != null) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(uri).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { Fallback(seed, cornerRadius) },
                error = { Fallback(seed, cornerRadius) },
            )
        }
    }
}

@Composable
private fun Fallback(seed: String?, cornerRadius: Dp) {
    if (seed != null) {
        WavelyArt(title = seed, modifier = Modifier.fillMaxSize(), cornerRadius = cornerRadius)
    } else {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxSize(0.42f),
            )
        }
    }
}
