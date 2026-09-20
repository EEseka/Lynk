package com.eeseka.lynk.shared.data.location

import com.eeseka.lynk.shared.domain.location.DeviceLocation
import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.Foundation.NSDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class CoreLocationDeviceLocation : DeviceLocation {

    private val locationManager = CLLocationManager()

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getLastKnownLocation(maxAge: Duration): LocationCoordinates? {
        val location = locationManager.location ?: return null
        if (location.age > maxAge) return null

        return location.coordinate.useContents {
            LocationCoordinates(
                latitude = latitude,
                longitude = longitude
            )
        }
    }

    override fun isLocationTurnedOn(): Boolean = CLLocationManager.locationServicesEnabled()
}

private val CLLocation.age: Duration
    get() = (NSDate().timeIntervalSinceReferenceDate - timestamp.timeIntervalSinceReferenceDate).seconds
