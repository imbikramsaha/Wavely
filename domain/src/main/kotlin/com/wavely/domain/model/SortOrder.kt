package com.wavely.domain.model

/** Per-list sort field. Direction carried separately by [SortSpec]. */
enum class SortField {
    TITLE,
    DATE_ADDED,
    DURATION,
    ALBUM,
    ARTIST,
}

enum class SortDirection { ASC, DESC }

data class SortSpec(
    val field: SortField = SortField.TITLE,
    val direction: SortDirection = SortDirection.ASC,
) {
    fun toggleDirection(): SortSpec =
        copy(direction = if (direction == SortDirection.ASC) SortDirection.DESC else SortDirection.ASC)
}

/** Identifies which list a [SortSpec] applies to, for per-list persistence (DataStore). */
enum class SortableList { TRACKS }
