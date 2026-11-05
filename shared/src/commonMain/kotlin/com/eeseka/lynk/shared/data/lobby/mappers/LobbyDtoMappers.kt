package com.eeseka.lynk.shared.data.lobby.mappers

import com.eeseka.lynk.shared.data.lobby.dto.ws.IncomingLobbyDto
import com.eeseka.lynk.shared.data.spot.mappers.toDomain
import com.eeseka.lynk.shared.domain.lobby.model.LiveLocation
import com.eeseka.lynk.shared.domain.lobby.model.VotingCandidate

fun IncomingLobbyDto.VotingCandidateDto.toDomain() = VotingCandidate(
    spot = spot.toDomain(),
    suggestedById = suggestedById,
    suggestedByDisplayName = suggestedByDisplayName,
    note = note
)

fun IncomingLobbyDto.LiveLocationDto.toDomain() = LiveLocation(
    userId = userId,
    latitude = latitude,
    longitude = longitude,
    sharedAt = sharedAt
)
