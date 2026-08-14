package com.eeseka.lynk.shared.data.lobby.dto

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketMessageDto(
    val type: String,
    val payload: String
)
