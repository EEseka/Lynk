package com.eeseka.lynk.shared.domain.payment.model

data class PayoutAccount(
    val bankName: String?,
    val accountNumberLast4: String,
    val accountHolderName: String
)
