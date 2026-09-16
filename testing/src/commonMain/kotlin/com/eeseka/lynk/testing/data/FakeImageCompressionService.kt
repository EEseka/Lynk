package com.eeseka.lynk.testing.data

import com.eeseka.lynk.shared.domain.media.ImageCompressionService

class FakeImageCompressionService : ImageCompressionService {
    var shouldFailCompress = false
    var shouldFailRead = false

    override suspend fun compress(contentPath: String, thresholdBytes: Long): String? {
        if (shouldFailCompress) return null
        return "compressed_$contentPath"
    }

    override suspend fun readBytes(imagePath: String): ByteArray? {
        if (shouldFailRead) return null
        return byteArrayOf(1, 2, 3)
    }
}
