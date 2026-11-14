package com.eeseka.lynk.core.data.hangout.dto

import com.eeseka.lynk.core.domain.hangout.model.PaymentState
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class HangoutPaymentDto(
    val totalCostKobo: Long,
    val costPerPersonKobo: Long,
    val splitHeadcount: Int,
    val deadline: Instant,
    val state: PaymentState
)