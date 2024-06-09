package com.mvnh.entities.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountLastTracks(@SerialName("yandex_track") val yandexTrack: String? = null,
                             @SerialName("spotify_track") val spotifyTrack: String? = null)