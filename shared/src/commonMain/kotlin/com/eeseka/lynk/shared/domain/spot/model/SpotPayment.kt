package com.eeseka.lynk.shared.domain.spot.model

data class SpotPayment(
    val acceptsCreditCards: Boolean?,
    val acceptsDebitCards: Boolean?,
    val acceptsCashOnly: Boolean?,
    val acceptsNfc: Boolean?
)
