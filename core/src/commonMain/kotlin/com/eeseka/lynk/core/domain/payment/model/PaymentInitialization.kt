package com.eeseka.lynk.core.domain.payment.model

data class PaymentInitialization(
    val authorizationUrl: String,
    val reference: String,
    val amountKobo: Long,
    val netAmountKobo: Long
)