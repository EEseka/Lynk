package com.eeseka.lynk.hangouts.presentation.util

import com.eeseka.lynk.shared.domain.hangout.model.PaymentState
import com.eeseka.lynk.shared.presentation.util.UiText
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.payment_state_awaiting_decision_guest
import lynk.feature.hangouts.generated.resources.payment_state_awaiting_decision_host
import lynk.feature.hangouts.generated.resources.payment_state_collecting_guest
import lynk.feature.hangouts.generated.resources.payment_state_collecting_host
import lynk.feature.hangouts.generated.resources.payment_state_paid_out_host
import lynk.feature.hangouts.generated.resources.payment_state_paying_out_host
import lynk.feature.hangouts.generated.resources.payment_state_payout_failed_host
import lynk.feature.hangouts.generated.resources.payment_state_ready_for_payout_host
import lynk.feature.hangouts.generated.resources.payment_state_settled_guest

fun PaymentState.toUiText(isHost: Boolean): UiText {
    val resource = when (this) {
        PaymentState.COLLECTING -> if (isHost) {
            Res.string.payment_state_collecting_host
        } else {
            Res.string.payment_state_collecting_guest
        }

        PaymentState.AWAITING_HOST_DECISION -> if (isHost) {
            Res.string.payment_state_awaiting_decision_host
        } else {
            Res.string.payment_state_awaiting_decision_guest
        }

        PaymentState.READY_FOR_PAYOUT -> if (isHost) {
            Res.string.payment_state_ready_for_payout_host
        } else {
            Res.string.payment_state_settled_guest
        }

        PaymentState.PAYING_OUT -> if (isHost) {
            Res.string.payment_state_paying_out_host
        } else {
            Res.string.payment_state_settled_guest
        }

        PaymentState.PAID_OUT -> if (isHost) {
            Res.string.payment_state_paid_out_host
        } else {
            Res.string.payment_state_settled_guest
        }

        PaymentState.PAYOUT_FAILED -> if (isHost) {
            Res.string.payment_state_payout_failed_host
        } else {
            Res.string.payment_state_settled_guest
        }
    }
    return UiText.Resource(resource)
}
