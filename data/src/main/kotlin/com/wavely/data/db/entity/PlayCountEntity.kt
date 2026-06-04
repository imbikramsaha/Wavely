package com.wavely.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Aggregate play count + last-played, for Most-played and History (design.md §7.2). */
@Entity(tableName = "play_counts")
data class PlayCountEntity(
    @PrimaryKey val trackKey: String,
    val count: Int,
    val lastPlayedAtMs: Long,
)
