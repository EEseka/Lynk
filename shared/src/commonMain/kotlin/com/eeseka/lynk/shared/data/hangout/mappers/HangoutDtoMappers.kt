package com.eeseka.lynk.shared.data.hangout.mappers

import com.eeseka.lynk.shared.data.hangout.dto.HangoutDto
import com.eeseka.lynk.shared.data.hangout.dto.HangoutParticipantDto
import com.eeseka.lynk.shared.data.hangout.dto.HangoutSummaryDto
import com.eeseka.lynk.shared.data.spot.mappers.toDomain
import com.eeseka.lynk.shared.domain.hangout.model.Hangout
import com.eeseka.lynk.shared.domain.hangout.model.HangoutParticipant
import com.eeseka.lynk.shared.domain.hangout.model.HangoutSummary

fun HangoutDto.toDomain(): Hangout {
    return Hangout(
        id = id,
        hostId = hostId,
        name = name,
        description = description,
        vibe = vibe,
        status = status,
        scheduledAt = scheduledAt,
        maxAttendees = maxAttendees,
        participantCount = participantCount,
        chosenSpot = chosenSpot?.toDomain(),
        totalCost = totalCost,
        participants = participants.map { it.toDomain() },
        createdAt = createdAt
    )
}

fun HangoutParticipantDto.toDomain(): HangoutParticipant {
    return HangoutParticipant(
        userId = userId,
        username = username,
        displayName = displayName,
        profilePictureUrl = profilePictureUrl,
        rsvpStatus = rsvpStatus,
        hasPaid = hasPaid
    )
}

fun HangoutSummaryDto.toDomain(): HangoutSummary {
    return HangoutSummary(
        id = id,
        hostId = hostId,
        name = name,
        description = description,
        vibe = vibe,
        status = status,
        scheduledAt = scheduledAt,
        maxAttendees = maxAttendees,
        participantCount = participantCount,
        hasChosenSpot = hasChosenSpot,
        totalCost = totalCost,
        createdAt = createdAt
    )
}