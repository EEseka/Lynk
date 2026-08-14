package com.eeseka.lynk.shared.domain.payment.model

import kotlin.time.Instant

data class PaymentSettings(
    val hangoutId: String,
    val totalCostKobo: Long,
    val costPerPersonKobo: Long,
    val paymentDeadline: Instant,
    val bankName: String?,
    val accountNumberLast4: String,
    val accountHolderName: String
)
