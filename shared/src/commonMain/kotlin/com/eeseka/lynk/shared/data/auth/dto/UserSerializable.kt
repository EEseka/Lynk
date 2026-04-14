package com.eeseka.lynk.shared.data.auth.dto

import com.eeseka.lynk.shared.domain.auth.model.AuthProvider
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