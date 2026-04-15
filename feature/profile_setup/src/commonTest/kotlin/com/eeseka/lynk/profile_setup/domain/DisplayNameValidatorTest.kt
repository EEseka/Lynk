package com.eeseka.lynk.profile_setup.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class DisplayNameValidatorTest {

    @Test
    fun `blank display name returns BLANK`() {
        assertThat(DisplayNameValidator.validate("")).isEqualTo(DisplayNameValidationState.BLANK)
        assertThat(DisplayNameValidator.validate("   ")).isEqualTo(DisplayNameValidationState.BLANK)
    }

    @Test
    fun `too long display name returns TOO_LONG`() {
        assertThat(DisplayNameValidator.validate("a".repeat(51))).isEqualTo(DisplayNameValidationState.TOO_LONG)
    }

    @Test
    fun `valid display name returns VALID`() {
        assertThat(DisplayNameValidator.validate("Valid Name")).isEqualTo(DisplayNameValidationState.VALID)
        assertThat(DisplayNameValidator.validate("John Doe 123")).isEqualTo(DisplayNameValidationState.VALID)
        assertThat(DisplayNameValidator.validate("A")).isEqualTo(DisplayNameValidationState.VALID)
    }
}