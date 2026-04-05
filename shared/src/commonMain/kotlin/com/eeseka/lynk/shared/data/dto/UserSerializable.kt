package com.eeseka.lynk.shared.data.dto

import kotlinx.serialization.Serializable

@Serializable
sealed interface UserSerializable {
    val id: String

    data class Guest(
        override val id: String
    ) : UserSerializable

    data class ProfileIncomplete(
        override val id: String,
        val email: String,
        val displayName: String,
        val profilePictureUrl: String? = null
    ) : UserSerializable

    data class Authenticated(
        override val id: String,
        val email: String,
        val displayName: String,
        val username: String,
        val profilePictureUrl: String? = null
    ) : UserSerializable
}
