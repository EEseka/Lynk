package com.eeseka.lynk.hangouts.presentation.util

import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Reads the photo the viewer already downloaded from Coil's disk cache, downloading it first if needed
suspend fun loadHangoutPhotoBytes(context: PlatformContext, url: String, cacheKey: String): ByteArray? {
    val imageLoader = SingletonImageLoader.get(context)
    val request = ImageRequest.Builder(context)
        .data(url)
        .memoryCacheKey(cacheKey)
        .diskCacheKey(cacheKey)
        .build()

    if (imageLoader.execute(request) is ErrorResult) return null
    val diskCache = imageLoader.diskCache ?: return null

    return withContext(Dispatchers.Default) {
        diskCache.openSnapshot(cacheKey)?.use { snapshot ->
            diskCache.fileSystem.read(snapshot.data) { readByteArray() }
        }
    }
}
