package com.eeseka.lynk.create_hangout.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.eeseka.lynk.create_hangout.domain.validation.HangoutDateValidationState
import com.eeseka.lynk.create_hangout.domain.validation.HangoutDateValidator
import kotlinx.datetime.LocalDate
import kotlin.test.Test

class HangoutDateValidatorTest {

    private val today = LocalDate(2026, 6, 4)

    @Test
    fun `null date returns MISSING`() {
        assertThat(HangoutDateValidator.validate(null, today)).isEqualTo(HangoutDateValidationState.MISSING)
    }

    @Test
    fun `past date returns PAST_DATE`() {
        assertThat(HangoutDateValidator.validate(LocalDate(2025, 1, 1), today)).isEqualTo(HangoutDateValidationState.PAST_DATE)
    }

    @Test
    fun `today returns VALID`() {
        assertThat(HangoutDateValidator.validate(today, today)).isEqualTo(HangoutDateValidationState.VALID)
    }

    @Test
    fun `future date returns VALID`() {
        assertThat(HangoutDateValidator.validate(LocalDate(2030, 12, 31), today)).isEqualTo(HangoutDateValidationState.VALID)
    }
}