package com.eeseka.lynk.shared.domain.auth

sealed interface User {
    val id: String

    data class Guest(
        override val id: String
    ) : User

    data class ProfileIncomplete(
        override val id: String,
        val email: String,
        val displayName: String,
        val profilePictureUrl: String? = null
    ) : User

    data class Authenticated(
        override val id: String,
        val email: String,
        val displayName: String,
        val username: String,
        val profilePictureUrl: String? = null
    ) : User
}