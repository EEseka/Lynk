package com.eeseka.lynk.shared.presentation.spot.util

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object DistanceCalculator {
    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Calculates the great-circle distance between two points on the Earth's surface.
     * Returns the distance in meters.
     */
    fun calculateDistanceInMeters(
        userLat: Double,
        userLng: Double,
        spotLat: Double,
        spotLng: Double
    ): Int {
        // Convert latitude and longitude from degrees to pure Kotlin radians
        val lat1Rad = userLat.toRadians()
        val lat2Rad = spotLat.toRadians()
        val deltaLat = (spotLat - userLat).toRadians()
        val deltaLng = (spotLng - userLng).toRadians()

        // Haversine formula
        val a = sin(deltaLat / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLng / 2).pow(2)

        val c = 2 * asin(sqrt(a))

        return (EARTH_RADIUS_METERS * c).roundToInt()
    }

    private fun Double.toRadians(): Double {
        return this * (PI / 180.0)
    }
}