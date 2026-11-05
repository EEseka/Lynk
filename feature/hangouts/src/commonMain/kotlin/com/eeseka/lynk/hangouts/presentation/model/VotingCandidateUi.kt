package com.eeseka.lynk.hangouts.presentation.model

import androidx.compose.runtime.Immutable
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi

@Immutable
data class VotingCandidateUi(
    val spot: SpotUi,
    val suggestedById: String,
    val suggestedByDisplayName: String,
    val note: String?
)
