package com.eeseka.lynk.profile_setup.domain

import com.eeseka.lynk.shared.domain.profile.ProfileConstants.MAX_USERNAME_LENGTH
import com.eeseka.lynk.shared.domain.profile.ProfileConstants.MIN_USERNAME_LENGTH

object UsernameValidator {

    fun validate(username: String): UsernameValidationState {
        val trimmed = username.trim()

        if (trimmed.isBlank()) return UsernameValidationState.BLANK
        if (trimmed.length < MIN_USERNAME_LENGTH) return UsernameValidationState.TOO_SHORT
        if (trimmed.length > MAX_USERNAME_LENGTH) return UsernameValidationState.TOO_LONG

        if (!trimmed.any { it.isAsciiLetter() }) return UsernameValidationState.NEEDS_LETTER
        if (trimmed.startsWith("_") || trimmed.endsWith("_")) return UsernameValidationState.EDGE_UNDERSCORE
        if (trimmed.contains("__")) return UsernameValidationState.CONSECUTIVE_UNDERSCORES

        // The Catch-All for weird symbols (@, !, spaces, accented letters, etc.), matching the server's a-z, A-Z, 0-9
        if (!trimmed.all { it.isAsciiLetter() || it in '0'..'9' || it == '_' }) {
            return UsernameValidationState.INVALID_CHARACTERS
        }

        return UsernameValidationState.VALID
    }

    private fun Char.isAsciiLetter() = this in 'a'..'z' || this in 'A'..'Z'
}

enum class UsernameValidationState {
    VALID,
    BLANK,
    TOO_SHORT,
    TOO_LONG,
    NEEDS_LETTER,
    EDGE_UNDERSCORE,
    CONSECUTIVE_UNDERSCORES,
    INVALID_CHARACTERS
}