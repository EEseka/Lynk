package com.eeseka.lynk.shared.data.payment.dto.requests

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ChangeDeadlineRequest(
    val newDeadline: Instant
)
