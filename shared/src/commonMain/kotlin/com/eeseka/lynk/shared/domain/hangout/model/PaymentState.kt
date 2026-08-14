package com.eeseka.lynk.shared.domain.hangout.model

enum class PaymentState {
    COLLECTING,
    AWAITING_HOST_DECISION,
    READY_FOR_PAYOUT,
    PAYING_OUT,
    PAID_OUT,
    PAYOUT_FAILED,
}