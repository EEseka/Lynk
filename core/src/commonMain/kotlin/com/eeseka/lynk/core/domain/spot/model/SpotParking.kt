package com.eeseka.lynk.core.domain.spot.model

data class SpotParking(
    val hasFreeLot: Boolean?,
    val hasPaidLot: Boolean?,
    val hasFreeStreet: Boolean?,
    val hasPaidStreet: Boolean?,
    val hasValet: Boolean?,
    val hasFreeGarage: Boolean?,
    val hasPaidGarage: Boolean?
)
