package com.eeseka.lynk.create_hangout.domain.validation

import com.eeseka.lynk.shared.domain.hangout.HangoutConstants.MAX_NAME_LENGTH

object HangoutNameValidator {

    fun validate(name: String): HangoutNameValidationState {
        val trimmed = name.trim()
        return when {
            trimmed.isBlank() -> HangoutNameValidationState.BLANK
            trimmed.length > MAX_NAME_LENGTH -> HangoutNameValidationState.TOO_LONG
            else -> HangoutNameValidationState.VALID
        }
    }
}

enum class HangoutNameValidationState {
    VALID,
    BLANK,
    TOO_LONG
}