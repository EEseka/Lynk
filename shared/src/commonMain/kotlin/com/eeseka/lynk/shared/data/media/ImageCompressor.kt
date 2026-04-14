package com.eeseka.lynk.shared.data.media

expect class ImageCompressor {
    suspend fun compress(contentPath: String, thresholdBytes: Long): String?
    suspend fun readBytes(imagePath: String): ByteArray?
}