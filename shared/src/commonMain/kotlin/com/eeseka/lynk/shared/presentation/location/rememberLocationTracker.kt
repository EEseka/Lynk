package com.eeseka.lynk.shared.presentation.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.icerock.moko.geo.compose.BindLocationTrackerEffect
import dev.icerock.moko.geo.compose.LocationTrackerAccuracy
import dev.icerock.moko.geo.compose.rememberLocationTrackerFactory

@Composable
fun rememberLocationController(): LocationController {
    val factory = rememberLocationTrackerFactory(LocationTrackerAccuracy.Best)
    val locationTracker = remember {
        factory.createLocationTracker()
    }

    BindLocationTrackerEffect(locationTracker)

    return remember {
        LocationController(locationTracker)
    }
}