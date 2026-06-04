package com.wavely.domain.model

/** Repeat cycle: Off → All → One (design.md §2.2 transport row). */
enum class RepeatMode {
    OFF,
    ALL,
    ONE,
    ;

    fun next(): RepeatMode = when (this) {
        OFF -> ALL
        ALL -> ONE
        ONE -> OFF
    }
}
