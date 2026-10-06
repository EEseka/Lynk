package com.eeseka.lynk.shared.data.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.eeseka.lynk.shared.domain.location.AreaNameResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume

class GeocoderAreaNameResolver(
    private val context: Context
) : AreaNameResolver {

    override suspend fun getAreaName(latitude: Double, longitude: Double): String? {
        // Some devices ship without a geocoding backend
        if (!Geocoder.isPresent()) return null

        val geocoder = Geocoder(context, Locale.getDefault())
        val address = try {
            findAddress(geocoder, latitude, longitude)
        } catch (_: IOException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }

        // The city wherever there is one, the state only when there's no city
        return address?.locality?.takeIf { it.isNotBlank() } ?: address?.adminArea?.takeIf { it.isNotBlank() }
    }

    private suspend fun findAddress(geocoder: Geocoder, latitude: Double, longitude: Double): Address? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        continuation.resume(addresses.firstOrNull())
                    }

                    override fun onError(errorMessage: String?) {
                        continuation.resume(null)
                    }
                })
            }
        }

        return withContext(Dispatchers.IO) {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
        }
    }
}
