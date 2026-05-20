package com.eeseka.lynk.shared.presentation.spot.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.compose.LocalPlatformContext
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
fun rememberGoogleImageRequest(url: String): ImageRequest? {
    val context = LocalPlatformContext.current
    return remember(url, context) {
        if (url.isBlank()) null
        else {
            val headers = NetworkHeaders.Builder().apply {
                getGoogleApiHeaders().forEach { (key, value) -> set(key, value) }
            }.build()

            ImageRequest.Builder(context)
                .data(url)
                .httpHeaders(headers)
                .crossfade(true)
                .build()
        }
    }
}