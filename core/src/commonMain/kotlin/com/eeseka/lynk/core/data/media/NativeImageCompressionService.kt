package com.eeseka.lynk.core.data.media

import com.eeseka.lynk.core.domain.media.ImageCompressionService

class NativeImageCompressionService(
    private val imageCompressor: ImageCompressor
) : ImageCompressionService {
    override suspend fun compress(
        contentPath: String,
        maxWidth: Int,
        thresholdBytes: Long
    ): String? {
        return imageCompressor.compress(contentPath, maxWidth, thresholdBytes)
    }

    override suspend fun readBytes(imagePath: String): ByteArray? {
        return imageCompressor.readBytes(imagePath)
    }
}