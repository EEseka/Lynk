package com.eeseka.lynk.shared.data.payment.dto

import com.eeseka.lynk.shared.domain.payment.model.PaymentStatus
import kotlinx.serialization.Serializable

@Serializable
data class PaymentVerificationDto(
    val status: PaymentStatus
)
