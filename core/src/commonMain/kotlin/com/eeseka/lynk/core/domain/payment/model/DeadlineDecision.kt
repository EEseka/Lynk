package com.eeseka.lynk.core.domain.payment.model

enum class DeadlineDecision {
    EXTEND,
    REMOVE_NON_PAYERS,
    PROCEED_ANYWAY,
    CANCEL
}