package com.eeseka.lynk.shared.data.location

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.SystemClock
import androidx.core.content.getSystemService
import androidx.core.location.LocationManagerCompat
import com.eeseka.lynk.shared.domain.location.DeviceLocation
import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

class PlayServicesDeviceLocation(
    private val context: Context
) : DeviceLocation {

    override suspend fun getLastKnownLocation(maxAge: Duration): LocationCoordinates? {
        return try {
            suspendCancellableCoroutine { continuation ->
                LocationServices.getFusedLocationProviderClient(context)
                    .lastLocation
                    .addOnSuccessListener { location ->
                        continuation.resume(
                            location?.takeIf { it.age <= maxAge }?.let {
                                LocationCoordinates(
                                    latitude = it.latitude,
                                    longitude = it.longitude
                                )
                            }
                        )
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            }
        } catch (_: SecurityException) {
            null
        }
    }

    override fun isLocationTurnedOn(): Boolean {
        val locationManager = context.getSystemService<LocationManager>() ?: return false
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }
}

/**
 * Measured against the time since boot rather than the wall clock, so changing the phone's
 * clock or a timezone jump cannot make a fresh fix look ancient.
 */
private val Location.age: Duration
    get() = (SystemClock.elapsedRealtimeNanos() - elapsedRealtimeNanos).nanoseconds
