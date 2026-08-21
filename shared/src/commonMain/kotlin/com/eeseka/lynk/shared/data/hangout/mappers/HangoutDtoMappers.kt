package com.eeseka.lynk.shared.data.hangout.mappers

import com.eeseka.lynk.shared.data.hangout.dto.HangoutDto
import com.eeseka.lynk.shared.data.hangout.dto.HangoutParticipantDto
import com.eeseka.lynk.shared.data.hangout.dto.HangoutPaymentDto
import com.eeseka.lynk.shared.data.hangout.dto.HangoutPreviewDto
import com.eeseka.lynk.shared.data.hangout.dto.HangoutStatsDto
import com.eeseka.lynk.shared.data.hangout.dto.HangoutSummaryDto
import com.eeseka.lynk.shared.data.hangout.dto.HangoutUserDto
import com.eeseka.lynk.shared.data.spot.mappers.toDomain
import com.eeseka.lynk.shared.domain.hangout.model.Hangout
import com.eeseka.lynk.shared.domain.hangout.model.HangoutParticipant
import com.eeseka.lynk.shared.domain.hangout.model.HangoutPayment
import com.eeseka.lynk.shared.domain.hangout.model.HangoutPreview
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStats
import com.eeseka.lynk.shared.domain.hangout.model.HangoutSummary
import com.eeseka.lynk.shared.domain.hangout.model.HangoutUser

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
        participants = participants.map { it.toDomain() },
        payment = payment?.toDomain(),
        createdAt = createdAt
    )
}

fun HangoutPaymentDto.toDomain(): HangoutPayment {
    return HangoutPayment(
        totalCostKobo = totalCostKobo,
        costPerPersonKobo = costPerPersonKobo,
        splitHeadcount = splitHeadcount,
        deadline = deadline,
        state = state
    )
}

fun HangoutParticipantDto.toDomain(): HangoutParticipant {
    return HangoutParticipant(
        user = user.toDomain(),
        rsvpStatus = rsvpStatus,
        hasPaid = hasPaid
    )
}

fun HangoutPreviewDto.toDomain(): HangoutPreview {
    return HangoutPreview(
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
        attendees = attendees.map { it.toDomain() },
        createdAt = createdAt
    )
}

fun HangoutUserDto.toDomain(): HangoutUser {
    return HangoutUser(
        userId = userId,
        username = username,
        displayName = displayName,
        profilePictureUrl = profilePictureUrl
    )
}

fun HangoutSummaryDto.toDomain(): HangoutSummary {
    return HangoutSummary(
        id = id,
        hostId = hostId,
        name = name,
        vibe = vibe,
        status = status,
        scheduledAt = scheduledAt,
        maxAttendees = maxAttendees,
        participantCount = participantCount,
        createdAt = createdAt
    )
}

fun HangoutStatsDto.toDomain(): HangoutStats {
    return HangoutStats(
        hostedCount = hostedCount,
        attendedCount = attendedCount
    )
}