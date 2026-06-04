package com.eeseka.lynk.shared.domain.hangout.model

import com.eeseka.lynk.shared.domain.spot.model.Spot
import kotlin.time.Instant

data class Hangout(
    val id: String,
    val hostId: String,
    val name: String,
    val description: String?,
    val vibe: HangoutVibe,
    val status: HangoutStatus,
    val scheduledAt: Instant,
    val maxAttendees: Int?, // null means unlimited
    val participantCount: Int,
    val chosenSpot: Spot?, // NULL if they are still voting
    val totalCost: Double?, // Host enters this when they know the budget/bill
    val participants: List<HangoutParticipant>,
    val createdAt: Instant
) {
    // Automatically splits the bill equally among everyone who RSVP'd "ATTENDING"
    val costPerPerson: Double?
        get() {
            if (totalCost == null) return null
            val attendingCount = participants.count { it.rsvpStatus == RsvpStatus.ATTENDING }
            return if (attendingCount == 0) null else totalCost / attendingCount
        }
}