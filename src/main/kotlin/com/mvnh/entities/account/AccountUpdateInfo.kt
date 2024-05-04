package com.mvnh.entities.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountUpdateInfo(val token: String,
                             @SerialName("visible_name") val visibleName: AccountVisibleName? = AccountVisibleName(),
                             @SerialName("music_preferences") val musicPreferences: List<String>? = null,
                             @SerialName("other_preferences") val otherPreferences: List<String>? = null,
                             val about: String? = null)