package com.eeseka.lynk.hangouts.presentation.hangout_detail

import com.eeseka.lynk.hangouts.presentation.hangout_detail.model.SearchTab
import com.eeseka.lynk.shared.domain.payment.model.DeadlineDecision
import kotlinx.datetime.LocalDate

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

    // Enable payments (host)
    data class OnCollectPaymentsToggled(val isOn: Boolean) : HangoutDetailAction
    data object OnPaymentDeadlinePickerClick : HangoutDetailAction
    data object OnDismissPaymentDeadlinePicker : HangoutDetailAction
    data class OnPaymentDeadlineSelected(val date: LocalDate) : HangoutDetailAction
    data object OnEnablePaymentsConfirmed : HangoutDetailAction

    // Deadline (host)
    data object OnChangeDeadlineClick : HangoutDetailAction
    data object OnDismissDeadlinePicker : HangoutDetailAction
    data class OnNewDeadlineSelected(val date: LocalDate) : HangoutDetailAction
    data object OnDeadlineDecisionClick : HangoutDetailAction
    data object OnDismissDeadlineDecisionSheet : HangoutDetailAction
    data class OnDeadlineDecisionSelected(val decision: DeadlineDecision) : HangoutDetailAction
    data object OnRetryPayoutClick : HangoutDetailAction

    // Paying (guest)
    data object OnPayClick : HangoutDetailAction
    data object OnDismissPayConfirmSheet : HangoutDetailAction
    data object OnConfirmPayment : HangoutDetailAction
    data object OnCheckPaymentClick : HangoutDetailAction
    data object OnDismissPaymentCheckout : HangoutDetailAction

    // Bank picker
    data object OnBankPickerClick : HangoutDetailAction
    data object OnDismissBankPicker : HangoutDetailAction
    data class OnBankSelected(val bankCode: String) : HangoutDetailAction
}