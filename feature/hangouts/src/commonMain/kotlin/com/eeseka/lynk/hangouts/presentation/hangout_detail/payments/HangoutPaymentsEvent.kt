package com.eeseka.lynk.hangouts.presentation.hangout_detail.payments

import com.eeseka.lynk.shared.presentation.util.UiText

sealed interface HangoutPaymentsEvent {
    data class Error(val message: UiText) : HangoutPaymentsEvent

    data object PaymentsEnabled : HangoutPaymentsEvent
    data object DeadlineChanged : HangoutPaymentsEvent
    data object DecisionSaved : HangoutPaymentsEvent
    data object PayoutQueued : HangoutPaymentsEvent

    data object PaymentPending : HangoutPaymentsEvent
    data object PaymentNotCompleted : HangoutPaymentsEvent
}
