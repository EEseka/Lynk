package com.eeseka.lynk.hangouts.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade

// Signed URLs change on every load, so the photo is cached by its own key, not by its url
@Composable
fun rememberHangoutPhotoRequest(url: String, cacheKey: String): ImageRequest {
    val context = LocalPlatformContext.current
    return remember(url, cacheKey) {
        ImageRequest.Builder(context)
            .data(url)
            .memoryCacheKey(cacheKey)
            .diskCacheKey(cacheKey)
            .crossfade(true)
            .build()
    }
}
