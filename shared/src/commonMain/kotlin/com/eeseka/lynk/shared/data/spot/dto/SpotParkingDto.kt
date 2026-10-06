package com.eeseka.lynk.shared.data.spot.dto

import kotlinx.serialization.Serializable

@Serializable
data class SpotParkingDto(
    val hasFreeLot: Boolean?,
    val hasPaidLot: Boolean?,
    val hasFreeStreet: Boolean?,
    val hasPaidStreet: Boolean?,
    val hasValet: Boolean?,
    val hasFreeGarage: Boolean?,
    val hasPaidGarage: Boolean?
)
