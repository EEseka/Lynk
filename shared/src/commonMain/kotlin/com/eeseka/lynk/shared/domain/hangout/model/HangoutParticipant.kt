package com.eeseka.lynk.shared.domain.hangout.model

data class HangoutParticipant(
    val userId: String,
    val username: String,
    val displayName: String,
    val profilePictureUrl: String?,

    // Hangout-specific data0
    val role: ParticipantRole,
    val rsvpStatus: RsvpStatus,
    val hasPaid: Boolean // For the Paystack integration
) {
    // Quick UI helper for avatars that lack a profile picture
    val initials: String
        get() = displayName.take(2).uppercase()
}