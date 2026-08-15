package com.eeseka.lynk.shared.data.media

import cnames.structs.__CFData
import com.eeseka.lynk.shared.domain.logging.LynkLogger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.useContents
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryGetValue
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFNumberCreate
import platform.CoreFoundation.CFNumberGetValue
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFNumberIntType
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToURL
import platform.ImageIO.CGImageSourceCopyPropertiesAtIndex
import platform.ImageIO.CGImageSourceCreateThumbnailAtIndex
import platform.ImageIO.CGImageSourceCreateWithData
import platform.ImageIO.CGImageSourceRef
import platform.ImageIO.kCGImagePropertyOrientation
import platform.ImageIO.kCGImagePropertyPixelHeight
import platform.ImageIO.kCGImagePropertyPixelWidth
import platform.ImageIO.kCGImageSourceCreateThumbnailFromImageAlways
import platform.ImageIO.kCGImageSourceCreateThumbnailWithTransform
import platform.ImageIO.kCGImageSourceThumbnailMaxPixelSize
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy
import kotlin.math.max
import kotlin.math.roundToInt

actual class ImageCompressor(
    private val logger: LynkLogger
) {

    private companion object {
        const val MAX_WIDTH = 1080.0
        const val START_QUALITY = 90
        const val MIN_QUALITY = 10
        const val QUALITY_STEP = 10
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun compress(contentPath: String, thresholdBytes: Long): String? {
        return withContext(Dispatchers.Default) {
            try {
                // Load Data
                val url = NSURL.URLWithString(contentPath) ?: return@withContext null
                val data = NSData.dataWithContentsOfURL(url) ?: return@withContext null

                // Decode already scaled down where possible, so a large photo is never
                // fully decoded into memory. Any failure falls back to a plain decode.
                val scaled = try {
                    decodeScaledToWidth(data)
                } catch (e: Exception) {
                    currentCoroutineContext().ensureActive()
                    logger.error("Scaled decode failed, falling back to full decode", e)
                    null
                }

                var image = scaled
                    ?: UIImage.imageWithData(data)
                    ?: run {
                        logger.error("ABORT: Could not decode image data")
                        return@withContext null
                    }

                currentCoroutineContext().ensureActive()

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

                currentCoroutineContext().ensureActive()

                // Compress Loop
                // Integer steps so the quality floor is exact, matching Android
                var quality = START_QUALITY
                var compressedData = UIImageJPEGRepresentation(image, quality / 100.0)

                while (
                    (compressedData?.length ?: 0UL).toLong() > thresholdBytes &&
                    quality > MIN_QUALITY
                ) {
                    currentCoroutineContext().ensureActive()
                    quality -= QUALITY_STEP
                    compressedData = UIImageJPEGRepresentation(image, quality / 100.0)
                }

                if (compressedData == null) {
                    logger.error("ABORT: JPEG encoding returned no data")
                    return@withContext null
                }

                if (compressedData.length.toLong() > thresholdBytes) {
                    logger.info("Image still above threshold at minimum quality")
                }

                // Save to Disk
                val fileName = "compressed_${NSUUID.UUID().UUIDString}.jpg"
                val newUrl = NSURL.fileURLWithPath(NSTemporaryDirectory())
                    .URLByAppendingPathComponent(fileName)

                if (newUrl == null) {
                    logger.error("ABORT: Could not build destination URL")
                    return@withContext null
                }

                if (!compressedData.writeToURL(newUrl, true)) {
                    logger.error("ABORT: Failed to write compressed image to disk")
                    return@withContext null
                }

                newUrl.absoluteString
            } catch (e: Exception) {
                currentCoroutineContext().ensureActive()
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
                currentCoroutineContext().ensureActive()
                null
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun decodeScaledToWidth(data: NSData): UIImage? {
        val cfData = CFBridgingRetain(data)?.reinterpret<__CFData>() ?: return null

        try {
            val source = CGImageSourceCreateWithData(cfData, null) ?: return null

            try {
                val pixelWidth = readImageProperty(source, kCGImagePropertyPixelWidth)
                val pixelHeight = readImageProperty(source, kCGImagePropertyPixelHeight)

                if (pixelWidth == null || pixelHeight == null) return null
                if (pixelWidth <= 0 || pixelHeight <= 0) return null

                // Camera photos store their pixels unrotated and carry the rotation in
                // EXIF. The thumbnail comes out rotated, so measure the rotated size.
                val orientation = readImageProperty(source, kCGImagePropertyOrientation) ?: 1
                val quarterTurned = orientation in 5..8
                val displayWidth = if (quarterTurned) pixelHeight else pixelWidth

                // Nothing to gain when the image is already within the cap
                if (displayWidth <= MAX_WIDTH) return null

                // The thumbnail cap applies to the longest edge, but only the width
                // is capped here, so scale the cap by the same ratio the width needs
                val scale = MAX_WIDTH / displayWidth
                val maxPixelSize = (max(pixelWidth, pixelHeight) * scale).roundToInt()
                if (maxPixelSize <= 0) return null

                return createThumbnail(source, maxPixelSize)
            } finally {
                CFRelease(source)
            }
        } finally {
            CFRelease(cfData)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun createThumbnail(source: CGImageSourceRef, maxPixelSize: Int): UIImage? {
        val options = CFDictionaryCreateMutable(
            kCFAllocatorDefault,
            3L,
            kCFTypeDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr
        ) ?: return null

        try {
            CFDictionarySetValue(
                options,
                kCGImageSourceCreateThumbnailFromImageAlways,
                kCFBooleanTrue
            )
            // Keeps the EXIF rotation of camera photos
            CFDictionarySetValue(
                options,
                kCGImageSourceCreateThumbnailWithTransform,
                kCFBooleanTrue
            )

            val sizeNumber = memScoped {
                val holder = alloc<IntVar>()
                holder.value = maxPixelSize
                CFNumberCreate(kCFAllocatorDefault, kCFNumberIntType, holder.ptr)
            } ?: return null

            try {
                CFDictionarySetValue(options, kCGImageSourceThumbnailMaxPixelSize, sizeNumber)

                val thumbnail = CGImageSourceCreateThumbnailAtIndex(
                    source,
                    0uL,
                    options
                ) ?: return null

                // UIImage retains the CGImage, so we drop our own reference
                try {
                    return UIImage.imageWithCGImage(thumbnail)
                } finally {
                    CGImageRelease(thumbnail)
                }
            } finally {
                CFRelease(sizeNumber)
            }
        } finally {
            CFRelease(options)
        }
    }

    // Reads the image header only, so the full image is never decoded here.
    @OptIn(ExperimentalForeignApi::class)
    private fun readImageProperty(source: CGImageSourceRef, key: CFStringRef?): Int? {
        val properties = CGImageSourceCopyPropertiesAtIndex(source, 0uL, null)
            ?: return null

        try {
            val value = CFDictionaryGetValue(properties, key) ?: return null

            return memScoped {
                val holder = alloc<IntVar>()
                val read = CFNumberGetValue(
                    value.reinterpret(),
                    kCFNumberIntType,
                    holder.ptr
                )
                if (read) holder.value else null
            }
        } finally {
            CFRelease(properties)
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