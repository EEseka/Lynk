package com.eeseka.lynk.hangouts.presentation.mappers

import com.eeseka.lynk.hangouts.presentation.model.VotingCandidateUi
import com.eeseka.lynk.shared.domain.lobby.model.VotingCandidate
import com.eeseka.lynk.shared.presentation.spot.mappers.toSpotUi

fun VotingCandidate.toVotingCandidateUi() = VotingCandidateUi(
    spot = spot.toSpotUi(),
    suggestedById = suggestedById,
    suggestedByDisplayName = suggestedByDisplayName,
    note = note
)
