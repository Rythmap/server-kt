package com.mvnh.entities.account

import com.mvnh.entities.music.yandex.YandexTrack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountLastTracks(@SerialName("yandex_track") val yandexTrack: YandexTrack? = null,
                             // @SerialName("spotify_track") val spotifyTrack: List<String>? = null
)