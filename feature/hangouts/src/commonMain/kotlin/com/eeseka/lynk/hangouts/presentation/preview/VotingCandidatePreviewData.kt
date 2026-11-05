package com.eeseka.lynk.hangouts.presentation.preview

import com.eeseka.lynk.hangouts.presentation.model.VotingCandidateUi
import com.eeseka.lynk.shared.presentation.preview.PREVIEW_GUEST_ID
import com.eeseka.lynk.shared.presentation.preview.PREVIEW_HOST_ID
import com.eeseka.lynk.shared.presentation.preview.previewSpots
import kotlinx.collections.immutable.toImmutableList

val previewVotingCandidates = previewSpots.mapIndexed { index, spot ->
    VotingCandidateUi(
        spot = spot,
        suggestedById = if (index % 2 == 0) PREVIEW_GUEST_ID else PREVIEW_HOST_ID,
        suggestedByDisplayName = if (index % 2 == 0) "Bola" else "Ada",
        note = if (index == 0) "Live band on Fridays and the jollof is unreal" else null
    )
}.toImmutableList()
