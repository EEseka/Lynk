package com.eeseka.lynk.assistant.presentation.model

import androidx.compose.runtime.Immutable
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi

@Immutable
data class AssistantRecommendationUi(
    val spot: SpotUi,
    val reason: String
)
