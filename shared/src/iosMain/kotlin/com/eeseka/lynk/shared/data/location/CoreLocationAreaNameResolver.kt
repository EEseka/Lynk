package com.eeseka.lynk.shared.data.location

import com.eeseka.lynk.shared.domain.location.AreaNameResolver
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLPlacemark
import kotlin.coroutines.resume

class CoreLocationAreaNameResolver : AreaNameResolver {

    override suspend fun getAreaName(latitude: Double, longitude: Double): String? {
        val geocoder = CLGeocoder()
        val location = CLLocation(latitude = latitude, longitude = longitude)

        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { geocoder.cancelGeocode() }

            geocoder.reverseGeocodeLocation(location) { placemarks, _ ->
                val placemark = placemarks?.firstOrNull() as? CLPlacemark
                // The city wherever there is one, the state only when there's no city
                continuation.resume(
                    placemark?.locality?.takeIf { it.isNotBlank() }
                        ?: placemark?.administrativeArea?.takeIf { it.isNotBlank() }
                )
            }
        }
    }
}
