package com.eeseka.lynk.shared.presentation.location

import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import dev.icerock.moko.geo.LocationTracker
import kotlinx.coroutines.flow.first

class LocationController(
    private val mokoTracker: LocationTracker
) {
    suspend fun startTracking() {
        mokoTracker.startTracking()
    }

    fun stopTracking() {
        mokoTracker.stopTracking()
    }

    suspend fun getCurrentLocation(): LocationCoordinates {
        val mokoLatLng = mokoTracker.getLocationsFlow().first()
        return LocationCoordinates(
            latitude = mokoLatLng.latitude,
            longitude = mokoLatLng.longitude
        )
    }
}