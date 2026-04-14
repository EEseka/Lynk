package com.eeseka.lynk.shared.data.media

import com.eeseka.lynk.shared.domain.media.ImageCompressionService

class NativeImageCompressionService(
    private val imageCompressor: ImageCompressor
) : ImageCompressionService {
    override suspend fun compress(contentPath: String, thresholdBytes: Long): String? {
        return imageCompressor.compress(contentPath, thresholdBytes)
    }

    override suspend fun readBytes(imagePath: String): ByteArray? {
        return imageCompressor.readBytes(imagePath)
    }
}