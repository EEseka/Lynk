package com.eeseka.lynk.shared.data.payment.dto

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PaymentSettingsDto(
    val hangoutId: String,
    val totalCostKobo: Long,
    val costPerPersonKobo: Long,
    val paymentDeadline: Instant,
    val bankName: String?,
    val accountNumberLast4: String,
    val accountHolderName: String
)
