package com.eeseka.lynk.hangouts.presentation.hangout_detail

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

    data class OnToggleSaveSpot(val spotId: String, val isCurrentlySaved: Boolean) : HangoutDetailAction
}