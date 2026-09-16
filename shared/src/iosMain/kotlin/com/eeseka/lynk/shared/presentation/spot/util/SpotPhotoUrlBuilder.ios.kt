package com.eeseka.lynk.shared.presentation.spot.util

import coil3.PlatformContext
import com.eeseka.lynk.AppConfig

actual fun getGooglePlacesApiKey() = AppConfig.GOOGLE_PLACES_IOS_API_KEY

actual fun getGoogleApiHeaders(context: PlatformContext): Map<String, String> {
    return mapOf(
        "X-Ios-Bundle-Identifier" to "com.eeseka.lynk.Lynk"
    )
}
