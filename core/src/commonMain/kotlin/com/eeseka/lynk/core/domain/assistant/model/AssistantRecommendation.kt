package com.eeseka.lynk.core.domain.assistant.model

import com.eeseka.lynk.core.domain.spot.model.Spot

data class AssistantRecommendation(
    val spot: Spot,
    val reason: String
)
