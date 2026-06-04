package com.wavely.domain.model

data class Playlist(
    val id: Long,
    val name: String,
    val trackCount: Int,
    /** Art URIs of the first few tracks, for a stacked thumbnail. */
    val artUris: List<String>,
)
