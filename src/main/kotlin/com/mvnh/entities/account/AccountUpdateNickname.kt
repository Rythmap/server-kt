package com.mvnh.entities.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountUpdateNickname(@SerialName("current_nickname") val currentNickname: String,
                                 @SerialName("new_nickname") val newNickname: String,
                                 val password: String)