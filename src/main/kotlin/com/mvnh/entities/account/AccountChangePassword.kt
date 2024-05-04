package com.mvnh.entities.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountChangePassword(val nickname: String,
                                 @SerialName("current_password") val currentPassword: String,
                                 @SerialName("new_password") val newPassword: String)