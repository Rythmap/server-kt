package com.mvnh.entities.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountInfoPublic(
    @SerialName("account_id") val accountID: String,
    val nickname: String,
    @SerialName("visible_name") val visibleName: AccountVisibleName?,
    val about: String? = null,
    @SerialName("music_preferences") val musicPreferences: List<String>? = null,
    @SerialName("other_preferences") val otherPreferences: List<String>? = null,
    @SerialName("last_tracks") val lastTracks: AccountLastTracks? = null,
    @SerialName("friends") val friends: List<String>? = null,
    @SerialName("created_at") val createdAt: String
)
