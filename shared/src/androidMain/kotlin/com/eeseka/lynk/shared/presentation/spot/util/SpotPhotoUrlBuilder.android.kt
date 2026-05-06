package com.eeseka.lynk.shared.presentation.spot.util

import com.eeseka.lynk.AppConfig

actual fun getGooglePlacesApiKey() = AppConfig.GOOGLE_PLACES_ANDROID_API_KEY

actual fun getGoogleApiHeaders(): Map<String, String> {
    return mapOf(
        "X-Android-Package" to "com.eeseka.lynk",
        "X-Android-Cert" to AppConfig.GOOGLE_PLACES_ANDROID_SHA1
    )
}