package com.eeseka.lynk.core.data.assistant.dto

import com.eeseka.lynk.core.data.spot.dto.SpotDto
import kotlinx.serialization.Serializable

@Serializable
data class AssistantRecommendationDto(
    val spot: SpotDto,
    val reason: String
)
