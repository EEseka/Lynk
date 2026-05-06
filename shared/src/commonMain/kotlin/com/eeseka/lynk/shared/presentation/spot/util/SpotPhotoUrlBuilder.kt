package com.eeseka.lynk.shared.presentation.spot.util

import kotlin.math.abs

object SpotPhotoUrlBuilder {
    private const val BASE_URL = "https://places.googleapis.com/v1"
    private const val MAX_DIMENSION = 1200

    // High-quality, free Unsplash images for development to prevent Google API charges
    private val mockImages = listOf(
        "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&q=80", // Restaurant
        "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=800&q=80", // Cafe
        "https://images.unsplash.com/photo-1514933651103-005eec06c04b?w=800&q=80", // Lounge
        "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800&q=80", // Dining
        "https://images.unsplash.com/photo-1525610553991-2bede1a236e2?w=800&q=80", // Coffee
        "https://images.unsplash.com/photo-1498654896293-37aacf113fd9?w=800&q=80", // Plated food
        "https://images.unsplash.com/photo-1559339352-11d035aa65de?w=800&q=80", // Cafe interior
        "https://images.unsplash.com/photo-1544148103-0773bf10d330?w=800&q=80", // Burger
        "https://images.unsplash.com/photo-1550966871-3ed3cdb5ed0c?w=800&q=80", // Outdoor seating
        "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=800&q=80"  // Food spread
    )

    /**
     * Converts a raw Google photo resource name into a loadable HTTP URL.
     * Example input: "places/ChIJ.../photos/Aaw..."
     */
    fun build(photoResourceName: String?): String? {
        if (photoResourceName.isNullOrBlank()) return null

        // TODO: Delete this mock block and uncomment the real API call below for production
        val hash = photoResourceName.hashCode()
        val index = abs(hash) % mockImages.size
        return mockImages[index]

//        val apiKey = getGooglePlacesApiKey()
//
//        return "$BASE_URL/$photoResourceName/media?maxHeightPx=$MAX_DIMENSION&maxWidthPx=$MAX_DIMENSION&key=$apiKey"
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