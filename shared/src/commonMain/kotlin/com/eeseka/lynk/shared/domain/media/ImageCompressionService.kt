package com.eeseka.lynk.shared.domain.media

interface ImageCompressionService {
    suspend fun compress(contentPath: String, thresholdBytes: Long = 300 * 1024L): String?
    suspend fun readBytes(imagePath: String): ByteArray?
}