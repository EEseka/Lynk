package com.eeseka.lynk.shared.domain.payment.model

enum class DeadlineDecision {
    EXTEND,
    REMOVE_NON_PAYERS,
    PROCEED_ANYWAY,
    CANCEL
}