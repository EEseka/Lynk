package com.eeseka.lynk.core.data.payment.dto

import kotlinx.serialization.Serializable

@Serializable
data class PayoutAccountDto(
    val bankName: String?,
    val accountNumberLast4: String,
    val accountHolderName: String
)
