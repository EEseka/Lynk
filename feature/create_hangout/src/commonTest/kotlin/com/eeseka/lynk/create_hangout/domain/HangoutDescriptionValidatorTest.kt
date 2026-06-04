package com.eeseka.lynk.create_hangout.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.eeseka.lynk.create_hangout.domain.validation.HangoutDescriptionValidationState
import com.eeseka.lynk.create_hangout.domain.validation.HangoutDescriptionValidator
import kotlin.test.Test

class HangoutDescriptionValidatorTest {

    @Test
    fun `null description returns VALID`() {
        assertThat(HangoutDescriptionValidator.validate(null)).isEqualTo(HangoutDescriptionValidationState.VALID)
    }

    @Test
    fun `blank description returns VALID`() {
        assertThat(HangoutDescriptionValidator.validate("   ")).isEqualTo(HangoutDescriptionValidationState.VALID)
    }

    @Test
    fun `description over 500 chars returns TOO_LONG`() {
        assertThat(HangoutDescriptionValidator.validate("a".repeat(501))).isEqualTo(HangoutDescriptionValidationState.TOO_LONG)
    }

    @Test
    fun `description exactly 500 chars returns VALID`() {
        assertThat(HangoutDescriptionValidator.validate("a".repeat(500))).isEqualTo(HangoutDescriptionValidationState.VALID)
    }

    @Test
    fun `normal description returns VALID`() {
        assertThat(HangoutDescriptionValidator.validate("Bringing the gang together")).isEqualTo(HangoutDescriptionValidationState.VALID)
    }
}