package com.mvnh.entities.account

import kotlinx.serialization.Serializable

@Serializable
data class AccountRegister(val nickname: String, val password: String, val email: String)