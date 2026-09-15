package com.eeseka.lynk.shared.presentation.spot.util

object SpotPhotoUrlBuilder {
    private const val BASE_URL = "https://places.googleapis.com/v1"
    private const val MAX_DIMENSION = 1200

    /**
     * Converts a raw Google photo resource name into a loadable HTTP URL.
     * Example input: "places/ChIJ.../photos/Aaw..."
     */
    fun build(photoResourceName: String?): String? {
        if (photoResourceName.isNullOrBlank()) return null

        val apiKey = getGooglePlacesApiKey()

        return "$BASE_URL/$photoResourceName/media?maxHeightPx=$MAX_DIMENSION&maxWidthPx=$MAX_DIMENSION&key=$apiKey"
    }

    /**
     * Convenience method to get the first photo from a list of resource names.
     */
    fun getPrimaryPhotoUrl(photoResourceNames: List<String>): String? {
        return build(photoResourceNames.firstOrNull())
    }
}

expect fun getGooglePlacesApiKey(): String

expect fun getGoogleApiHeaders(): Map<String, String>