package com.eeseka.lynk.shared.data.payment.dto

import kotlinx.serialization.Serializable

@Serializable
data class BankAccountDto(
    val accountNumber: String,
    val accountName: String,
    val bankCode: String
)
