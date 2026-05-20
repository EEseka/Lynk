package com.eeseka.lynk.create_hangout.domain.validation

import kotlinx.datetime.LocalDate

object HangoutDateValidator {
    fun validate(selectedDate: LocalDate?, today: LocalDate): HangoutDateValidationState {
        if (selectedDate == null) return HangoutDateValidationState.MISSING
        
        return if (selectedDate < today) {
            HangoutDateValidationState.PAST_DATE
        } else {
            HangoutDateValidationState.VALID
        }
    }
}

enum class HangoutDateValidationState {
    VALID,
    MISSING,
    PAST_DATE
}