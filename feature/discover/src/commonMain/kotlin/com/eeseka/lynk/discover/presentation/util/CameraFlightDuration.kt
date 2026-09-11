package com.eeseka.lynk.discover.presentation.util

import com.eeseka.lynk.shared.presentation.spot.util.DistanceCalculator
import org.maplibre.compose.camera.CameraPosition
import kotlin.math.abs
import kotlin.math.log10
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

fun CameraPosition.flightDurationTo(destination: CameraPosition): Duration {
    val distanceMeters = DistanceCalculator.calculateDistanceInMeters(
        userLat = target.latitude,
        userLng = target.longitude,
        spotLat = destination.target.latitude,
        spotLng = destination.target.longitude
    )
    val kilometres = distanceMeters / 1000.0
    val zoomChange = abs(destination.zoom - zoom)
    val millis = 800 + 500 * log10(1 + kilometres) + 50 * zoomChange
    return millis.coerceIn(800.0, 3000.0).milliseconds
}
