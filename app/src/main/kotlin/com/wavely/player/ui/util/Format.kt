package com.wavely.player.ui.util

import java.util.Locale

/** Formats a duration in ms as m:ss or h:mm:ss. Unknown/negative → "0:00". */
fun formatDuration(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) {
        String.format(Locale.US, "%d:%02d:%02d", h, m, s)
    } else {
        String.format(Locale.US, "%d:%02d", m, s)
    }
}

/** Remaining time as a negative label, GP-style (design.md §2.2 seekbar). */
fun formatRemaining(positionMs: Long, durationMs: Long): String {
    val remaining = (durationMs - positionMs).coerceAtLeast(0)
    return "-" + formatDuration(remaining)
}

/** "Artist · Album" / "Artist · 3:58" secondary line. */
fun secondaryLine(vararg parts: String?): String =
    parts.filterNot { it.isNullOrBlank() }.joinToString(" · ")

/** Correctly pluralized track count: "1 track" / "N tracks". */
fun pluralTracks(count: Int): String = if (count == 1) "1 track" else "$count tracks"

/** Playback speed label, e.g. "1x", "1.5x", "0.75x". */
fun formatSpeed(speed: Float): String {
    val s = if (speed % 1f == 0f) speed.toInt().toString() else speed.toString().trimEnd('0').trimEnd('.')
    return "${s}x"
}
