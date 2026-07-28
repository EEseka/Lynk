package com.eeseka.lynk.shared.domain.hangout.model

data class HangoutParticipant(
    val userId: String,
    val username: String,
    val displayName: String,
    val profilePictureUrl: String?,

    // Hangout-specific data
    val rsvpStatus: RsvpStatus,
    val hasPaid: Boolean // For the Paystack integration
)

// TODO: Later maybe model HangoutParticipant well by storing HangoutUser as that contains the first four data