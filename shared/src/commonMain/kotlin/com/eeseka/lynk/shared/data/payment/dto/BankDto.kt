package com.eeseka.lynk.shared.data.payment.dto

import kotlinx.serialization.Serializable

@Serializable
data class BankDto(
    val name: String,
    val code: String,
    val logoUrl: String?
)
