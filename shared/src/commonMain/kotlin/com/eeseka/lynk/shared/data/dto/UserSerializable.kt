package com.eeseka.lynk.shared.data.dto

import com.eeseka.lynk.shared.domain.auth.AuthProvider
import kotlinx.serialization.Serializable

@Serializable
data class UserSerializable(
    val id: String,
    val authProvider: AuthProvider,
    val email: String? = null,
    val displayName: String? = null,
    val username: String? = null,
    val profilePhotoUrl: String? = null
)