package com.eeseka.lynk.shared.data.lobby.dto.ws

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketMessageDto(
    val type: String,
    val payload: String
)
