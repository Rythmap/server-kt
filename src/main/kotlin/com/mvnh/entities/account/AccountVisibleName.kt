package com.mvnh.entities.account

import kotlinx.serialization.Serializable

@Serializable
data class AccountVisibleName(val name: String? = null, val surname: String? = null)