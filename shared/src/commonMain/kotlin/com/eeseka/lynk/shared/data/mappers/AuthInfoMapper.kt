package com.eeseka.lynk.shared.data.mappers

import com.eeseka.lynk.shared.data.dto.AuthInfoSerializable
import com.eeseka.lynk.shared.data.dto.UserSerializable
import com.eeseka.lynk.shared.domain.auth.AuthInfo
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
    return when (this) {
        is UserSerializable.Guest -> User.Guest(
            id = id
        )

        is UserSerializable.ProfileIncomplete -> User.ProfileIncomplete(
            id = id,
            email = email,
            displayName = displayName,
            profilePictureUrl = profilePictureUrl
        )

        is UserSerializable.Authenticated -> User.Authenticated(
            id = id,
            email = email,
            displayName = displayName,
            username = username,
            profilePictureUrl = profilePictureUrl
        )
    }
}

fun User.toSerializable(): UserSerializable {
    return when (this) {
        is User.Guest -> UserSerializable.Guest(
            id = id
        )

        is User.ProfileIncomplete -> UserSerializable.ProfileIncomplete(
            id = id,
            email = email,
            displayName = displayName,
            profilePictureUrl = profilePictureUrl
        )

        is User.Authenticated -> UserSerializable.Authenticated(
            id = id,
            email = email,
            displayName = displayName,
            username = username,
            profilePictureUrl = profilePictureUrl
        )
    }
}