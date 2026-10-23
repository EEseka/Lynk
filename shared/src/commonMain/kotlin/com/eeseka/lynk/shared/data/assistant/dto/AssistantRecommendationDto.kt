package com.eeseka.lynk.shared.data.assistant.dto

import com.eeseka.lynk.shared.data.spot.dto.SpotDto
import kotlinx.serialization.Serializable

@Serializable
data class AssistantRecommendationDto(
    val spot: SpotDto,
    val reason: String
)
