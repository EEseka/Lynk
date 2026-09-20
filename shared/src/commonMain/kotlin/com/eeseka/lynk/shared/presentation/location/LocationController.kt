package com.eeseka.lynk.shared.presentation.location

import com.eeseka.lynk.shared.domain.location.DeviceLocation
import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import com.eeseka.lynk.shared.domain.location.LocationError
import com.eeseka.lynk.shared.domain.util.Result
import dev.icerock.moko.geo.LocationTracker
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.RequestCanceledException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class LocationController(
    private val mokoTracker: LocationTracker,
    private val deviceLocation: DeviceLocation
) {
    /**
     * Emits the fix the phone already has so the map is useful straight away, then emits the
     * sharper one once the tracker finds it. A failure is only reported when neither arrived.
     */
    fun observeCurrentLocation(): Flow<Result<LocationCoordinates, LocationError>> = flow {
        var hasEmittedLocation = false

        deviceLocation.getLastKnownLocation(MAX_CACHED_FIX_AGE)?.let { cachedLocation ->
            emit(Result.Success(cachedLocation))
            hasEmittedLocation = true
        }

        if (!deviceLocation.isLocationTurnedOn()) {
            if (!hasEmittedLocation) emit(Result.Failure(LocationError.LOCATION_TURNED_OFF))
            return@flow
        }

        try {
            mokoTracker.startTracking()
            val mokoLatLng = withTimeoutOrNull(FIX_TIMEOUT) {
                mokoTracker.getLocationsFlow().first()
            }
            if (mokoLatLng != null) {
                emit(
                    Result.Success(
                        LocationCoordinates(
                            latitude = mokoLatLng.latitude,
                            longitude = mokoLatLng.longitude
                        )
                    )
                )
            } else if (!hasEmittedLocation) {
                emit(Result.Failure(LocationError.NO_FIX_IN_TIME))
            }
        } catch (_: DeniedException) {
            if (!hasEmittedLocation) emit(Result.Failure(LocationError.PERMISSION_DENIED))
        } catch (_: RequestCanceledException) {
            if (!hasEmittedLocation) emit(Result.Failure(LocationError.REQUEST_CANCELLED))
        } finally {
            mokoTracker.stopTracking()
        }
    }

    companion object {
        private val FIX_TIMEOUT = 25.seconds
        // Older than this and the phone could have been in another town since.
        private val MAX_CACHED_FIX_AGE = 1.hours
    }
}
