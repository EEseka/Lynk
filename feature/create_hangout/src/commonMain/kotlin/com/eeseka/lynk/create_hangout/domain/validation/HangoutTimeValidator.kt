package com.eeseka.lynk.create_hangout.domain.validation

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

object HangoutTimeValidator {
    fun validate(
        selectedTime: LocalTime?,
        selectedDate: LocalDate?,
        currentDate: LocalDate,
        currentTime: LocalTime
    ): HangoutTimeValidationState {
        if (selectedTime == null) return HangoutTimeValidationState.MISSING
        
        // If they haven't picked a date yet, or picked a bad date, let the DateValidator handle it.
        // We assume the time itself is fine for now coz we do not want double errors
        if (selectedDate == null || selectedDate < currentDate) {
            return HangoutTimeValidationState.VALID 
        }

        if (selectedDate == currentDate && selectedTime <= currentTime) {
            return HangoutTimeValidationState.PAST_TIME
        }

        return HangoutTimeValidationState.VALID
    }
}

enum class HangoutTimeValidationState {
    VALID,
    MISSING,
    PAST_TIME
}