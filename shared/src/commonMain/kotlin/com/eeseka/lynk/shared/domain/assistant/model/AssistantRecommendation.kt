package com.eeseka.lynk.shared.domain.assistant.model

import com.eeseka.lynk.shared.domain.spot.model.Spot

data class AssistantRecommendation(
    val spot: Spot,
    val reason: String
)
