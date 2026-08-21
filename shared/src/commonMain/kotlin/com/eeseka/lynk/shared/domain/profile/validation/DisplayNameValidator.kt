package com.eeseka.lynk.shared.domain.profile.validation

object DisplayNameValidator {
    private const val MAX_LENGTH = 50

    fun validate(displayName: String): DisplayNameValidationState {
        val trimmed = displayName.trim()
        return when {
            trimmed.isBlank() -> DisplayNameValidationState.BLANK
            trimmed.length > MAX_LENGTH -> DisplayNameValidationState.TOO_LONG
            else -> DisplayNameValidationState.VALID
        }
    }
}

enum class DisplayNameValidationState {
    VALID,
    BLANK,
    TOO_LONG
}