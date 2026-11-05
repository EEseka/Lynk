package com.eeseka.lynk.shared.domain.lobby.model

import com.eeseka.lynk.shared.domain.spot.model.Spot

data class VotingCandidate(
    val spot: Spot,
    val suggestedById: String,
    val suggestedByDisplayName: String,
    val note: String?
)
