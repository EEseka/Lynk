package com.eeseka.lynk.shared.presentation.hangout.model

import androidx.compose.runtime.Stable
import com.eeseka.lynk.shared.domain.hangout.model.PaymentState
import kotlin.time.Instant

@Stable
data class HangoutPaymentUi(
    val totalCostKobo: Long,
    val costPerPersonKobo: Long,
    val splitHeadcount: Int,
    val deadline: Instant,
    val state: PaymentState
)