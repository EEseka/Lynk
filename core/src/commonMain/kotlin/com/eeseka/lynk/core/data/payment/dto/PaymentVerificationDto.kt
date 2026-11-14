package com.eeseka.lynk.core.data.payment.dto

import com.eeseka.lynk.core.domain.payment.model.PaymentStatus
import kotlinx.serialization.Serializable

@Serializable
data class PaymentVerificationDto(
    val status: PaymentStatus
)
