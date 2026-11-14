package com.eeseka.lynk.core.data.payment.dto.requests

import com.eeseka.lynk.core.domain.payment.model.DeadlineDecision
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class DeadlineDecisionRequest(
    val decision: DeadlineDecision,
    val newDeadline: Instant? = null
)
