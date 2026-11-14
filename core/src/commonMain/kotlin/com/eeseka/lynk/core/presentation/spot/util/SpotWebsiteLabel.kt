package com.eeseka.lynk.core.presentation.spot.util

// "https://www.instagram.com/nokbyalara/?hl=en" reads "instagram.com/nokbyalara"
fun String.toWebsiteLabel(): String {
    return substringAfter("://")
        .removePrefix("www.")
        .substringBefore("?")
        .substringBefore("#")
        .trimEnd('/')
}
