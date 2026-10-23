package com.eeseka.lynk.shared.domain.profile.validation

import com.eeseka.lynk.shared.domain.profile.ProfileConstants.MAX_DISPLAY_NAME_LENGTH

object DisplayNameValidator {

    fun validate(displayName: String): DisplayNameValidationState {
        val trimmed = displayName.trim()
        return when {
            trimmed.isBlank() -> DisplayNameValidationState.BLANK
            trimmed.length > MAX_DISPLAY_NAME_LENGTH -> DisplayNameValidationState.TOO_LONG
            else -> DisplayNameValidationState.VALID
        }
    }
}

enum class DisplayNameValidationState {
    VALID,
    BLANK,
    TOO_LONG
}