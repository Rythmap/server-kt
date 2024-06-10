package com.mvnh.entities.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountAuthResponse(@SerialName("account_id") val accountID: String,
                               val token: String,
                               @SerialName("token_type") val tokenType: String = "bearer")
