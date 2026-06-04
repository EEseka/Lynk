package com.eeseka.lynk.create_hangout.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.eeseka.lynk.create_hangout.domain.validation.HangoutTimeValidationState
import com.eeseka.lynk.create_hangout.domain.validation.HangoutTimeValidator
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test

class HangoutTimeValidatorTest {

    private val today = LocalDate(2026, 6, 4)
    private val tomorrow = LocalDate(2026, 6, 5)
    private val currentTime = LocalTime(14, 0)

    @Test
    fun `null time returns MISSING`() {
        assertThat(
            HangoutTimeValidator.validate(null, today, today, currentTime)
        ).isEqualTo(HangoutTimeValidationState.MISSING)
    }

    @Test
    fun `same day time in past returns PAST_TIME`() {
        assertThat(
            HangoutTimeValidator.validate(LocalTime(13, 59), today, today, currentTime)
        ).isEqualTo(HangoutTimeValidationState.PAST_TIME)
    }

    @Test
    fun `same day time equal to current returns PAST_TIME`() {
        assertThat(
            HangoutTimeValidator.validate(currentTime, today, today, currentTime)
        ).isEqualTo(HangoutTimeValidationState.PAST_TIME)
    }

    @Test
    fun `same day time in future returns VALID`() {
        assertThat(
            HangoutTimeValidator.validate(LocalTime(14, 1), today, today, currentTime)
        ).isEqualTo(HangoutTimeValidationState.VALID)
    }

    @Test
    fun `future date with any time returns VALID`() {
        assertThat(
            HangoutTimeValidator.validate(LocalTime(1, 0), tomorrow, today, currentTime)
        ).isEqualTo(HangoutTimeValidationState.VALID)
    }

    @Test
    fun `past selected date defers to DateValidator and returns VALID`() {
        val pastDate = LocalDate(2020, 1, 1)
        assertThat(
            HangoutTimeValidator.validate(LocalTime(10, 0), pastDate, today, currentTime)
        ).isEqualTo(HangoutTimeValidationState.VALID)
    }

    @Test
    fun `null selected date returns VALID`() {
        assertThat(
            HangoutTimeValidator.validate(LocalTime(10, 0), null, today, currentTime)
        ).isEqualTo(HangoutTimeValidationState.VALID)
    }
}