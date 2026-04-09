package com.eeseka.lynk.shared.data.mappers

import com.eeseka.lynk.shared.data.dto.AuthInfoSerializable
import com.eeseka.lynk.shared.data.dto.UserSerializable
import com.eeseka.lynk.shared.domain.auth.AuthInfo
import com.eeseka.lynk.shared.domain.auth.AuthProvider
import com.eeseka.lynk.shared.domain.auth.User

fun AuthInfoSerializable.toDomain(): AuthInfo {
    return AuthInfo(
        accessToken = accessToken,
        refreshToken = refreshToken,
        user = user.toDomain()
    )
}

fun AuthInfo.toSerializable(): AuthInfoSerializable {
    return AuthInfoSerializable(
        accessToken = accessToken,
        refreshToken = refreshToken,
        user = user.toSerializable()
    )
}

fun UserSerializable.toDomain(): User {
    return when (authProvider) {
        AuthProvider.GUEST -> User.Guest(id = id)

        AuthProvider.GOOGLE, AuthProvider.APPLE -> {
            val safeEmail =
                requireNotNull(email) { "Server violation: Email is null for non-guest" }

            if (username == null) {
                User.ProfileIncomplete(
                    id = id,
                    provider = authProvider,
                    email = safeEmail,
                    displayName = displayName,
                    profilePictureUrl = profilePhotoUrl
                )
            } else {
                val safeName =
                    requireNotNull(displayName) { "Server violation: Display name missing for Authenticated user" }

                User.Authenticated(
                    id = id,
                    provider = authProvider,
                    email = safeEmail,
                    displayName = safeName,
                    username = username,
                    profilePictureUrl = profilePhotoUrl
                )
            }
        }
    }
}

fun User.toSerializable(): UserSerializable {
    return when (this) {
        is User.Guest -> UserSerializable(
            id = id,
            authProvider = provider
        )

        is User.ProfileIncomplete -> UserSerializable(
            id = id,
            authProvider = provider,
            email = email,
            displayName = displayName,
            profilePhotoUrl = profilePictureUrl
        )

        is User.Authenticated -> UserSerializable(
            id = id,
            authProvider = provider,
            email = email,
            displayName = displayName,
            username = username,
            profilePhotoUrl = profilePictureUrl
        )
    }
}