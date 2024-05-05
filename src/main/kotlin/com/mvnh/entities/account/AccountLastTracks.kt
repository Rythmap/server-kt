package com.mvnh.entities.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountLastTracks(@SerialName("yandex_track_id") val yandexTrackID: String? = null,
                             @SerialName("spotify_track_id") val spotifyTrackID: String? = null)