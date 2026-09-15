package com.eeseka.lynk.hangouts.presentation.hangout_detail.payments

import com.eeseka.lynk.shared.domain.payment.model.DeadlineDecision
import kotlinx.datetime.LocalDate

sealed interface HangoutPaymentsAction {
    data class OnSelectHangout(val hangoutId: String?) : HangoutPaymentsAction

    // Enable payments (host)
    data class OnCollectPaymentsToggled(val isOn: Boolean) : HangoutPaymentsAction
    data object OnPaymentDeadlinePickerClick : HangoutPaymentsAction
    data object OnDismissPaymentDeadlinePicker : HangoutPaymentsAction
    data class OnPaymentDeadlineSelected(val date: LocalDate) : HangoutPaymentsAction
    data object OnEnablePaymentsConfirmed : HangoutPaymentsAction

    // Deadline (host)
    data object OnChangeDeadlineClick : HangoutPaymentsAction
    data object OnDismissDeadlinePicker : HangoutPaymentsAction
    data class OnNewDeadlineSelected(val date: LocalDate) : HangoutPaymentsAction
    data object OnDeadlineDecisionClick : HangoutPaymentsAction
    data object OnDismissDeadlineDecisionSheet : HangoutPaymentsAction
    data class OnDeadlineDecisionSelected(val decision: DeadlineDecision) : HangoutPaymentsAction
    data object OnRetryPayoutClick : HangoutPaymentsAction

    // Paying (guest)
    data object OnPayClick : HangoutPaymentsAction
    data object OnDismissPayConfirmSheet : HangoutPaymentsAction
    data object OnConfirmPayment : HangoutPaymentsAction
    data object OnCheckPaymentClick : HangoutPaymentsAction
    data object OnDismissPaymentCheckout : HangoutPaymentsAction

    // Bank picker
    data object OnBankPickerClick : HangoutPaymentsAction
    data object OnDismissBankPicker : HangoutPaymentsAction
    data class OnBankSelected(val bankCode: String) : HangoutPaymentsAction
}
