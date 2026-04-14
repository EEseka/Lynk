package com.eeseka.lynk.profile_setup.data

import com.eeseka.lynk.shared.domain.media.ImageCompressionService

class FakeImageCompressionService : ImageCompressionService {
    var shouldFailRead = false

    override suspend fun compress(contentPath: String, thresholdBytes: Long): String {
        return "compressed_$contentPath"
    }

    override suspend fun readBytes(imagePath: String): ByteArray? {
        if (shouldFailRead) return null
        return byteArrayOf(1, 2, 3)
    }
}