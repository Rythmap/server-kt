package com.mvnh.entities.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountRegister(val nickname: String,
                           @SerialName("visible_name") val visibleName: String? = null,
                           val password: String,
                           val email: String,
                           @SerialName("music_preferences") val musicPreferences: List<String>? = null,
                           @SerialName("other_preferences") val otherPreferences: List<String>? = null,
                           val about: String? = null)