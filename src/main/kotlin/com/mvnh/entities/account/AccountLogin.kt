package com.mvnh.entities.account

import kotlinx.serialization.Serializable

@Serializable
data class AccountLogin(val login: String, val password: String)