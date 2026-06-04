package com.wavely.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Persisted queue entry for cold-start session restore (design.md §10.3). */
@Entity(tableName = "session_queue")
data class SessionQueueEntity(
    @PrimaryKey val position: Int,
    val trackKey: String,
)

/** Single-row session state. Always id = 0. */
@Entity(tableName = "session_state")
data class SessionStateEntity(
    @PrimaryKey val id: Int = 0,
    val index: Int,
    val positionMs: Long,
    val shuffle: Boolean,
    val repeatOrdinal: Int,
    val speed: Float,
)
