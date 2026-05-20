package com.eeseka.lynk.create_hangout.domain.validation

object HangoutDescriptionValidator {
    private const val MAX_LENGTH = 500

    fun validate(description: String?): HangoutDescriptionValidationState {
        if (description.isNullOrBlank()) return HangoutDescriptionValidationState.VALID
        
        return when {
            description.trim().length > MAX_LENGTH -> HangoutDescriptionValidationState.TOO_LONG
            else -> HangoutDescriptionValidationState.VALID
        }
    }
}

enum class HangoutDescriptionValidationState {
    VALID,
    TOO_LONG
}