package com.eeseka.lynk.create_hangout.domain.validation

object HangoutNameValidator {
    private const val MAX_LENGTH = 50

    fun validate(name: String): HangoutNameValidationState {
        val trimmed = name.trim()
        return when {
            trimmed.isBlank() -> HangoutNameValidationState.BLANK
            trimmed.length > MAX_LENGTH -> HangoutNameValidationState.TOO_LONG
            else -> HangoutNameValidationState.VALID
        }
    }
}

enum class HangoutNameValidationState {
    VALID,
    BLANK,
    TOO_LONG
}