package com.eeseka.lynk.shared.domain.payment.model

data class PaymentInitialization(
    val authorizationUrl: String,
    val reference: String,
    val amountKobo: Long,
    val netAmountKobo: Long
)