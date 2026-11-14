package com.eeseka.lynk.core.data.payment.dto

import kotlinx.serialization.Serializable

@Serializable
data class BankDto(
    val name: String,
    val code: String,
    val logoUrl: String?
)
