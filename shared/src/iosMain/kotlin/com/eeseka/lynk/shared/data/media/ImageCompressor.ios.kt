package com.eeseka.lynk.shared.data.media

import com.eeseka.lynk.shared.domain.logging.LynkLogger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToURL
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy

actual class ImageCompressor(
    private val logger: LynkLogger
) {

    private companion object {
        const val MAX_WIDTH = 1080.0
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun compress(contentPath: String, thresholdBytes: Long): String? {
        return withContext(Dispatchers.Default) {
            try {
                // Load Data
                val url = NSURL.URLWithString(contentPath) ?: return@withContext null
                val data = NSData.dataWithContentsOfURL(url) ?: return@withContext null
                var image = UIImage(data = data)

                // RESIZE
                // We use useContents to safely read the struct values
                val (currentWidth, currentHeight) = image.size.useContents { width to height }

                // Safety check: Avoid divide by zero
                if (currentWidth > 0 && currentHeight > 0 && currentWidth > MAX_WIDTH) {

                    // Switch to Main Thread for drawing (Required for UIKit)
                    val newImage = withContext(Dispatchers.Main) {
                        image.resize(MAX_WIDTH, currentWidth, currentHeight)
                    }

                    if (newImage != null) {
                        image = newImage
                    }
                }

                // Compress Loop
                var quality = 0.9
                var compressedData: NSData?

                do {
                    compressedData = UIImageJPEGRepresentation(image, quality)
                    quality -= 0.1
                } while ((compressedData?.length ?: 0UL).toLong() > thresholdBytes && quality > 0.1)

                // Save to Disk
                if (compressedData != null) {
                    val fileName = "compressed_${NSUUID.UUID().UUIDString}.jpg"
                    val newUrl = NSURL.fileURLWithPath(NSTemporaryDirectory())
                        .URLByAppendingPathComponent(fileName)

                    if (newUrl != null) {
                        compressedData.writeToURL(newUrl, true)
                        return@withContext newUrl.absoluteString
                    }
                }
                null
            } catch (e: Exception) {
                logger.error("Image Compression Failed: ${e.message}", e)
                null
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun readBytes(imagePath: String): ByteArray? {
        return withContext(Dispatchers.Default) {
            try {
                val url = NSURL.URLWithString(imagePath) ?: return@withContext null
                val data = NSData.dataWithContentsOfURL(url) ?: return@withContext null
                val bytes = ByteArray(data.length.toInt())
                if (bytes.isNotEmpty()) {
                    memcpy(bytes.refTo(0), data.bytes, data.length)
                }
                bytes
            } catch (_: Exception) {
                null
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun UIImage.resize(
        targetWidth: Double,
        currentWidth: Double,
        currentHeight: Double
    ): UIImage? {
        // Calculate Aspect Ratio safely
        if (currentWidth == 0.0) return null

        val aspectRatio = currentHeight / currentWidth
        val targetHeight = targetWidth * aspectRatio

        // Final sanity check
        if (targetHeight <= 0) return null

        val targetSize = CGSizeMake(targetWidth, targetHeight)

        val format = UIGraphicsImageRendererFormat.defaultFormat()
        format.scale = 1.0
        val renderer = UIGraphicsImageRenderer(size = targetSize, format = format)

        return renderer.imageWithActions {
            this.drawInRect(CGRectMake(0.0, 0.0, targetWidth, targetHeight))
        }
    }
}