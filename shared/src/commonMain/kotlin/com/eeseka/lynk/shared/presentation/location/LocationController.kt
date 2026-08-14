package com.eeseka.lynk.shared.presentation.location

import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import dev.icerock.moko.geo.LocationTracker
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.RequestCanceledException
import kotlinx.coroutines.flow.first

class LocationController(
    private val mokoTracker: LocationTracker
) {
    suspend fun getCurrentLocation(): LocationCoordinates? {
        return try {
            mokoTracker.startTracking()
            val mokoLatLng = mokoTracker.getLocationsFlow().first()
            LocationCoordinates(
                latitude = mokoLatLng.latitude,
                longitude = mokoLatLng.longitude
            )
        } catch (_: DeniedException) {
            null
        } catch (_: RequestCanceledException) {
            null
        } finally {
            mokoTracker.stopTracking()
        }
    }
}