package com.eeseka.lynk.create_hangout.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.eeseka.lynk.create_hangout.domain.validation.HangoutNameValidationState
import com.eeseka.lynk.create_hangout.domain.validation.HangoutNameValidator
import kotlin.test.Test

class HangoutNameValidatorTest {

    @Test
    fun `blank name returns BLANK`() {
        assertThat(HangoutNameValidator.validate("")).isEqualTo(HangoutNameValidationState.BLANK)
        assertThat(HangoutNameValidator.validate("   ")).isEqualTo(HangoutNameValidationState.BLANK)
    }

    @Test
    fun `name over 50 chars returns TOO_LONG`() {
        assertThat(HangoutNameValidator.validate("a".repeat(51))).isEqualTo(HangoutNameValidationState.TOO_LONG)
    }

    @Test
    fun `name exactly 50 chars returns VALID`() {
        assertThat(HangoutNameValidator.validate("a".repeat(50))).isEqualTo(HangoutNameValidationState.VALID)
    }

    @Test
    fun `normal name returns VALID`() {
        assertThat(HangoutNameValidator.validate("Pizza Night")).isEqualTo(HangoutNameValidationState.VALID)
    }

    @Test
    fun `name with surrounding whitespace trims before length check`() {
        // 50 real chars + 2 spaces = 52 total, but trim brings it to 50 → VALID
        assertThat(HangoutNameValidator.validate(" ${"a".repeat(50)} ")).isEqualTo(HangoutNameValidationState.VALID)
    }
}