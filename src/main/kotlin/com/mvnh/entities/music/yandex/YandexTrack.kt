package com.mvnh.entities.music.yandex

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YandexTrack(
    @SerialName("track_id") val trackId: Int,
    val title: String,
    val artist: String,
    val img: String,
    val duration: Int,
    val minutes: Int,
    val seconds: Int,
    val album: Int,
    @SerialName("download_link") val downloadLink: String
)