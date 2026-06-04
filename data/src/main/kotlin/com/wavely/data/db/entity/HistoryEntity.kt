package com.wavely.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Append-only play history (design.md §1.2 Activity → History tab). */
@Entity(tableName = "history", indices = [Index("playedAtMs")])
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackKey: String,
    val playedAtMs: Long,
)
