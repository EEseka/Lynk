package com.eeseka.lynk.core.domain.lobby.model

import com.eeseka.lynk.core.domain.spot.model.Spot

data class VotingCandidate(
    val spot: Spot,
    val suggestedById: String,
    val suggestedByDisplayName: String,
    val note: String?
)
