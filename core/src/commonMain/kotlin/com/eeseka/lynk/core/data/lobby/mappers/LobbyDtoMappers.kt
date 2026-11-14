package com.eeseka.lynk.core.data.lobby.mappers

import com.eeseka.lynk.core.data.lobby.dto.ws.IncomingLobbyDto
import com.eeseka.lynk.core.data.spot.mappers.toDomain
import com.eeseka.lynk.core.domain.lobby.model.LiveLocation
import com.eeseka.lynk.core.domain.lobby.model.VotingCandidate

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
