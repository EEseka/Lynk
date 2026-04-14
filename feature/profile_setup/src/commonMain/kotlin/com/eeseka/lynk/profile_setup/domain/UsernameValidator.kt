package com.eeseka.lynk.profile_setup.domain

object UsernameValidator {
    private const val MIN_LENGTH = 3
    private const val MAX_LENGTH = 20

    fun validate(username: String): UsernameValidationState {
        val trimmed = username.trim()

        if (trimmed.isBlank()) return UsernameValidationState.BLANK
        if (trimmed.length < MIN_LENGTH) return UsernameValidationState.TOO_SHORT
        if (trimmed.length > MAX_LENGTH) return UsernameValidationState.TOO_LONG

        if (!trimmed.any { it.isLetter() }) return UsernameValidationState.NEEDS_LETTER
        if (trimmed.startsWith("_") || trimmed.endsWith("_")) return UsernameValidationState.EDGE_UNDERSCORE
        if (trimmed.contains("__")) return UsernameValidationState.CONSECUTIVE_UNDERSCORES

        // The Catch-All for weird symbols (@, !, spaces, etc.)
        if (!trimmed.all { it.isLetterOrDigit() || it == '_' }) return UsernameValidationState.INVALID_CHARACTERS

        return UsernameValidationState.VALID
    }
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