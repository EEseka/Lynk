package com.eeseka.lynk.core.data.payment.dto

import kotlinx.serialization.Serializable

@Serializable
data class PaymentInitializationDto(
    val authorizationUrl: String,
    val reference: String,
    val amountKobo: Long,
    val netAmountKobo: Long
)
