package com.eeseka.lynk.shared.data.payment.dto.requests

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class EnablePaymentsRequest(
    val totalCostKobo: Long,
    val paymentDeadline: Instant,
    val accountNumber: String,
    val bankCode: String
)
