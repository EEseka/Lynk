package com.eeseka.lynk.shared.presentation.location

import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import dev.icerock.moko.geo.LocationTracker
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.RequestCanceledException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

class LocationController(
    private val mokoTracker: LocationTracker
) {
    suspend fun getCurrentLocation(): LocationCoordinates? {
        return try {
            mokoTracker.startTracking()
            val mokoLatLng = withTimeoutOrNull(FIX_TIMEOUT) {
                mokoTracker.getLocationsFlow().first()
            }
            mokoLatLng?.let {
                LocationCoordinates(
                    latitude = it.latitude,
                    longitude = it.longitude
                )
            }
        } catch (_: DeniedException) {
            null
        } catch (_: RequestCanceledException) {
            null
        } finally {
            mokoTracker.stopTracking()
        }
    }

    companion object {
        private val FIX_TIMEOUT = 10.seconds
    }
}