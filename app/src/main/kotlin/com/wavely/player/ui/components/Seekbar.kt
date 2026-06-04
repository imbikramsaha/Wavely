package com.wavely.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wavely.player.ui.theme.brandGradient
import com.wavely.player.ui.util.formatDuration
import com.wavely.player.ui.util.formatRemaining

private val Track = Color(0x38FFFFFF)        // white 22%
private val Labels = Color(0xB8FFFFFF)       // white 72%

/**
 * Now Playing seekbar — brand-gradient active track + white thumb on the dark ambient surface.
 * Dragging pauses the auto-updating position and seeks on release (design.md §2.2).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Seekbar(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableStateOf(0f) }

    val max = durationMs.coerceAtLeast(1).toFloat()
    val sliderValue = if (dragging) dragValue else positionMs.coerceIn(0, durationMs).toFloat()
    val shownPosition = if (dragging) dragValue.toLong() else positionMs
    val frac = (sliderValue / max).coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = sliderValue,
            onValueChange = {
                dragging = true
                dragValue = it
            },
            onValueChangeFinished = {
                onSeek(dragValue.toLong())
                dragging = false
            },
            valueRange = 0f..max,
            colors = SliderDefaults.colors(thumbColor = Color.White),
            thumb = {
                Box(Modifier.size(14.dp).clip(CircleShape).background(Color.White))
            },
            track = {
                Box(
                    Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Track),
                ) {
                    Box(
                        Modifier.fillMaxWidth(frac).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(brandGradient),
                    )
                }
            },
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = formatDuration(shownPosition),
                style = MaterialTheme.typography.bodySmall,
                color = Labels,
            )
            Text(
                text = formatRemaining(shownPosition, durationMs),
                style = MaterialTheme.typography.bodySmall,
                color = Labels,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
            )
        }
    }
}
