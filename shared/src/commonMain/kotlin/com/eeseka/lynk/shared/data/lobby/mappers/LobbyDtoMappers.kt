package com.eeseka.lynk.shared.data.lobby.mappers

import com.eeseka.lynk.shared.data.lobby.dto.ws.IncomingLobbyDto
import com.eeseka.lynk.shared.domain.lobby.model.LiveLocation

fun IncomingLobbyDto.LiveLocationDto.toDomain() = LiveLocation(
    userId = userId,
    latitude = latitude,
    longitude = longitude,
    sharedAt = sharedAt
)
