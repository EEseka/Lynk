package com.eeseka.lynk.shared.presentation.hangout.mappers

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.eeseka.lynk.shared.domain.hangout.model.HangoutUser
import kotlin.test.Test

class HangoutUserInitialsTest {

    @Test
    fun `two names give the first letter of each`() {
        assertThat(initialsOf("Emma Eseka")).isEqualTo("EE")
    }

    @Test
    fun `more than two names use the first and the last`() {
        assertThat(initialsOf("ada chioma obi")).isEqualTo("AO")
    }

    @Test
    fun `one name gives its first two letters`() {
        assertThat(initialsOf("emma")).isEqualTo("EM")
    }

    @Test
    fun `one letter name gives that letter`() {
        assertThat(initialsOf("e")).isEqualTo("E")
    }

    @Test
    fun `extra spaces are ignored`() {
        assertThat(initialsOf("  Emma    Eseka  ")).isEqualTo("EE")
    }

    @Test
    fun `blank name gives no initials`() {
        assertThat(initialsOf("")).isEqualTo("")
        assertThat(initialsOf("   ")).isEqualTo("")
    }

    private fun initialsOf(displayName: String): String {
        return HangoutUser(
            userId = "user-1",
            username = "user",
            displayName = displayName,
            profilePictureUrl = null
        ).toHangoutUserUi().initials
    }
}
