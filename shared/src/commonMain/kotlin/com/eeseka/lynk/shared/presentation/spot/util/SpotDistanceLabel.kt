package com.eeseka.lynk.shared.presentation.spot.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.km
import lynk.shared.generated.resources.m
import org.jetbrains.compose.resources.stringResource

/**
 * How far a spot is from the viewer in a straight line. Under a kilometre it reads in metres
 * ("800 m"), otherwise in kilometres ("1.9 km", "2 km"). Null if the location is unknown.
 */
@Composable
fun rememberSpotDistanceLabel(
    userLatitude: Double?,
    userLongitude: Double?,
    spotLatitude: Double,
    spotLongitude: Double
): String? {
    val kilometreSuffix = stringResource(Res.string.km)
    val metreSuffix = stringResource(Res.string.m)

    return remember(userLatitude, userLongitude, spotLatitude, spotLongitude) {
        if (userLatitude == null || userLongitude == null) return@remember null

        val meters = DistanceCalculator.calculateDistanceInMeters(
            userLat = userLatitude,
            userLng = userLongitude,
            spotLat = spotLatitude,
            spotLng = spotLongitude
        )

        if (meters < 1000) return@remember "$meters $metreSuffix"

        val tenthsOfKilometre = (meters + 50) / 100
        val wholeKilometres = tenthsOfKilometre / 10
        val remainingTenth = tenthsOfKilometre % 10

        if (remainingTenth == 0) {
            "$wholeKilometres $kilometreSuffix"
        } else {
            "$wholeKilometres.$remainingTenth $kilometreSuffix"
        }
    }
}
