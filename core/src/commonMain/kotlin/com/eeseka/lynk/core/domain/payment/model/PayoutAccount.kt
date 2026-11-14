package com.eeseka.lynk.core.domain.payment.model

data class PayoutAccount(
    val bankName: String?,
    val accountNumberLast4: String,
    val accountHolderName: String
)
