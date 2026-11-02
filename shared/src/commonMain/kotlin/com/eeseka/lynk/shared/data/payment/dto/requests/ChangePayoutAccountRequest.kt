package com.eeseka.lynk.shared.data.payment.dto.requests

import kotlinx.serialization.Serializable

@Serializable
data class ChangePayoutAccountRequest(
    val accountNumber: String,
    val bankCode: String
)
