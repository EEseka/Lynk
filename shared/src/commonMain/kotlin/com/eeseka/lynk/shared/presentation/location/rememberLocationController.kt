package com.eeseka.lynk.shared.presentation.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.icerock.moko.geo.compose.BindLocationTrackerEffect
import dev.icerock.moko.geo.compose.LocationTrackerAccuracy
import dev.icerock.moko.geo.compose.rememberLocationTrackerFactory

/**
 * Balanced lets Wi-Fi and cell towers answer, which works indoors and in seconds.
 * Precise is GPS only: sharper, but it can take a minute outdoors and may never arrive inside.
 */
enum class LocationAccuracy {
    Balanced,
    Precise
}

@Composable
fun rememberLocationController(
    accuracy: LocationAccuracy = LocationAccuracy.Balanced
): LocationController {
    val trackerAccuracy = when (accuracy) {
        LocationAccuracy.Balanced -> LocationTrackerAccuracy.Medium
        LocationAccuracy.Precise -> LocationTrackerAccuracy.Best
    }

    val factory = rememberLocationTrackerFactory(trackerAccuracy)
    val locationTracker = remember { factory.createLocationTracker() }
    val deviceLocation = rememberDeviceLocation()

    BindLocationTrackerEffect(locationTracker)

    return remember {
        LocationController(locationTracker, deviceLocation)
    }
}
