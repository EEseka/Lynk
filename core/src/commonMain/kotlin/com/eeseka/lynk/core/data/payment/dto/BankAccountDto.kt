package com.eeseka.lynk.core.data.payment.dto

import kotlinx.serialization.Serializable

@Serializable
data class BankAccountDto(
    val accountNumber: String,
    val accountHolderName: String,
    val bankCode: String
)
