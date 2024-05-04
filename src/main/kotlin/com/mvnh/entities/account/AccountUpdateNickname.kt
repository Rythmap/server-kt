package com.mvnh.entities.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountUpdateNickname(val token: String,
                                 @SerialName("new_nickname") val newNickname: String)