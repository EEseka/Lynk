package com.eeseka.lynk.profile_setup.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class UsernameValidatorTest {

    @Test
    fun `blank username returns BLANK`() {
        assertThat(UsernameValidator.validate("")).isEqualTo(UsernameValidationState.BLANK)
        assertThat(UsernameValidator.validate("   ")).isEqualTo(UsernameValidationState.BLANK)
    }

    @Test
    fun `too short username returns TOO_SHORT`() {
        assertThat(UsernameValidator.validate("ab")).isEqualTo(UsernameValidationState.TOO_SHORT)
    }

    @Test
    fun `too long username returns TOO_LONG`() {
        assertThat(UsernameValidator.validate("a".repeat(21))).isEqualTo(UsernameValidationState.TOO_LONG)
    }

    @Test
    fun `username without letters returns NEEDS_LETTER`() {
        assertThat(UsernameValidator.validate("12345")).isEqualTo(UsernameValidationState.NEEDS_LETTER)
        assertThat(UsernameValidator.validate("_____")).isEqualTo(UsernameValidationState.NEEDS_LETTER)
    }

    @Test
    fun `username with edge underscore returns EDGE_UNDERSCORE`() {
        assertThat(UsernameValidator.validate("_user")).isEqualTo(UsernameValidationState.EDGE_UNDERSCORE)
        assertThat(UsernameValidator.validate("user_")).isEqualTo(UsernameValidationState.EDGE_UNDERSCORE)
    }

    @Test
    fun `username with consecutive underscores returns CONSECUTIVE_UNDERSCORES`() {
        assertThat(UsernameValidator.validate("user__name")).isEqualTo(UsernameValidationState.CONSECUTIVE_UNDERSCORES)
    }

    @Test
    fun `username with invalid characters returns INVALID_CHARACTERS`() {
        assertThat(UsernameValidator.validate("user!name")).isEqualTo(UsernameValidationState.INVALID_CHARACTERS)
        assertThat(UsernameValidator.validate("user name")).isEqualTo(UsernameValidationState.INVALID_CHARACTERS)
        assertThat(UsernameValidator.validate("user@home")).isEqualTo(UsernameValidationState.INVALID_CHARACTERS)
    }

    @Test
    fun `valid username returns VALID`() {
        assertThat(UsernameValidator.validate("valid_user")).isEqualTo(UsernameValidationState.VALID)
        assertThat(UsernameValidator.validate("user123")).isEqualTo(UsernameValidationState.VALID)
        assertThat(UsernameValidator.validate("a_b_c")).isEqualTo(UsernameValidationState.VALID)
    }
}