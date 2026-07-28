package com.eeseka.lynk.hangouts.presentation.hangout_detail

import com.eeseka.lynk.hangouts.presentation.hangout_detail.model.SearchTab

sealed interface HangoutDetailAction {
    data class OnSelectHangout(val hangoutId: String?) : HangoutDetailAction
    data object OnRetryClick : HangoutDetailAction

    // Host actions
    data object OnCompleteHangoutConfirmed : HangoutDetailAction
    data object OnCancelHangoutConfirmed : HangoutDetailAction
    data class OnWithdrawParticipantInvite(val userId: String) : HangoutDetailAction

    // Attendee action (host equivalent is cancel)
    data object OnLeaveHangoutConfirmed : HangoutDetailAction

    // Invite flow
    data object OnInviteClick : HangoutDetailAction
    data object OnDismissInviteSheet : HangoutDetailAction
    data class OnInviteUser(val userId: String) : HangoutDetailAction

    // Voting
    data class OnCastVote(val spotId: String) : HangoutDetailAction
    data object OnCloseVotingClick : HangoutDetailAction
    data class OnBreakTie(val spotId: String) : HangoutDetailAction
    data class OnShareLocation(val latitude: Double, val longitude: Double) : HangoutDetailAction

    // Propose-spot sheet
    data object OnProposeSpotClick : HangoutDetailAction
    data object OnDismissProposeSpotSheet : HangoutDetailAction
    data class OnTabSelected(val tab: SearchTab) : HangoutDetailAction
    data object LoadNextSpotPage : HangoutDetailAction
    data object LoadNextFavoriteSpotPage : HangoutDetailAction
    data class OnProposeSpot(val spotId: String) : HangoutDetailAction
    data class OnRemoveSpot(val spotId: String) : HangoutDetailAction

    data class OnToggleSaveSpot(val spotId: String, val isCurrentlySaved: Boolean) : HangoutDetailAction
}