package com.eeseka.lynk.hangouts.presentation.hangout_detail.voting

import com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.model.SearchTab

sealed interface HangoutVotingAction {
    data class OnSelectHangout(val hangoutId: String?) : HangoutVotingAction

    // Voting
    data class OnCastVote(val spotId: String) : HangoutVotingAction
    data object OnCloseVotingClick : HangoutVotingAction
    data class OnBreakTie(val spotId: String) : HangoutVotingAction
    data class OnShareLocation(val latitude: Double, val longitude: Double) : HangoutVotingAction

    // Propose-spot sheet
    data object OnProposeSpotClick : HangoutVotingAction
    data object OnDismissProposeSpotSheet : HangoutVotingAction
    data class OnTabSelected(val tab: SearchTab) : HangoutVotingAction
    data object LoadNextSpotPage : HangoutVotingAction
    data object LoadNextFavoriteSpotPage : HangoutVotingAction
    data class OnProposeSpot(val spotId: String) : HangoutVotingAction
    data class OnRemoveSpot(val spotId: String) : HangoutVotingAction
}
