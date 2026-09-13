package com.eeseka.lynk.hangouts.presentation.hangout_detail.voting

import com.eeseka.lynk.shared.presentation.util.UiText

sealed interface HangoutVotingEvent {
    data class Error(val message: UiText) : HangoutVotingEvent
    data object SpotSuggested : HangoutVotingEvent
    data object VotingTied : HangoutVotingEvent
}
