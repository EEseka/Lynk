package com.eeseka.lynk.shared.data.auth.mappers

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.eeseka.lynk.shared.data.auth.dto.UserSerializable
import com.eeseka.lynk.shared.domain.auth.model.AuthProvider
import com.eeseka.lynk.shared.domain.auth.model.User
import kotlin.test.Test
import kotlin.test.assertFailsWith

class AuthInfoMapperTest {

    @Test
    fun `guest provider becomes a guest user`() {
        val saved = UserSerializable(id = "user-1", authProvider = AuthProvider.GUEST)

        assertThat(saved.toDomain()).isEqualTo(User.Guest(id = "user-1"))
    }

    @Test
    fun `signed in user without a username has an incomplete profile`() {
        val saved = UserSerializable(
            id = "user-1",
            authProvider = AuthProvider.GOOGLE,
            email = "emma@example.com",
            displayName = "Emma",
            profilePhotoUrl = "https://example.com/emma.jpg"
        )

        assertThat(saved.toDomain()).isEqualTo(
            User.ProfileIncomplete(
                id = "user-1",
                provider = AuthProvider.GOOGLE,
                email = "emma@example.com",
                displayName = "Emma",
                profilePictureUrl = "https://example.com/emma.jpg"
            )
        )
    }

    @Test
    fun `incomplete profile may have no display name yet`() {
        val saved = UserSerializable(
            id = "user-1",
            authProvider = AuthProvider.APPLE,
            email = "emma@example.com"
        )

        assertThat(saved.toDomain()).isEqualTo(
            User.ProfileIncomplete(
                id = "user-1",
                provider = AuthProvider.APPLE,
                email = "emma@example.com",
                displayName = null
            )
        )
    }

    @Test
    fun `signed in user with a username is authenticated`() {
        val saved = UserSerializable(
            id = "user-1",
            authProvider = AuthProvider.GOOGLE,
            email = "emma@example.com",
            displayName = "Emma",
            username = "emma"
        )

        assertThat(saved.toDomain()).isEqualTo(
            User.Authenticated(
                id = "user-1",
                provider = AuthProvider.GOOGLE,
                email = "emma@example.com",
                displayName = "Emma",
                username = "emma"
            )
        )
    }

    @Test
    fun `signed in user without an email is refused`() {
        val saved = UserSerializable(id = "user-1", authProvider = AuthProvider.GOOGLE, username = "emma")

        assertFailsWith<IllegalArgumentException> { saved.toDomain() }
    }

    @Test
    fun `authenticated user without a display name is refused`() {
        val saved = UserSerializable(
            id = "user-1",
            authProvider = AuthProvider.GOOGLE,
            email = "emma@example.com",
            username = "emma"
        )

        assertFailsWith<IllegalArgumentException> { saved.toDomain() }
    }

    @Test
    fun `every kind of user comes back the same after saving`() {
        val users = listOf(
            User.Guest(id = "guest-1"),
            User.ProfileIncomplete(
                id = "user-2",
                provider = AuthProvider.APPLE,
                email = "ada@example.com",
                displayName = "Ada",
                profilePictureUrl = "https://example.com/ada.jpg"
            ),
            User.Authenticated(
                id = "user-3",
                provider = AuthProvider.GOOGLE,
                email = "emma@example.com",
                displayName = "Emma",
                username = "emma",
                profilePictureUrl = "https://example.com/emma.jpg"
            )
        )

        users.forEach { user ->
            assertThat(user.toSerializable().toDomain()).isEqualTo(user)
        }
    }
}
