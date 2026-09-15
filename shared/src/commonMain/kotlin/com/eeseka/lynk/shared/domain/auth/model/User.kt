package com.eeseka.lynk.shared.domain.auth.model

sealed interface User {
    val id: String
    val provider: AuthProvider

    data class Guest(
        override val id: String
    ) : User {
        override val provider = AuthProvider.GUEST
    }

    data class ProfileIncomplete(
        override val id: String,
        override val provider: AuthProvider,
        val email: String,
        val displayName: String?,
        val profilePictureUrl: String? = null
    ) : User

    data class Authenticated(
        override val id: String,
        override val provider: AuthProvider,
        val email: String,
        val displayName: String,
        val username: String,
        val profilePictureUrl: String? = null
    ) : User
}