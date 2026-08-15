package com.eeseka.lynk.shared.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.graphics.scale
import androidx.core.net.toUri
import com.eeseka.lynk.shared.domain.logging.LynkLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import kotlin.math.roundToInt

actual class ImageCompressor(private val context: Context, private val logger: LynkLogger) {

    private companion object {
        const val MAX_WIDTH = 1080
    }

    actual suspend fun compress(contentPath: String, thresholdBytes: Long): String? {
        return withContext(Dispatchers.IO) {
            try {
                val cleanPath = contentPath.trim()

                // STREAM OPENER
                fun openStream(): InputStream? {
                    try {
                        if (cleanPath.startsWith("file://")) {
                            val rawPath = cleanPath.removePrefix("file://")
                            val file = File(rawPath)
                            if (file.exists() && file.canRead()) {
                                return FileInputStream(file)
                            }
                        }
                        val uri = cleanPath.toUri()
                        return context.contentResolver.openInputStream(uri)
                    } catch (e: Exception) {
                        logger.error("Error opening stream", e)
                        return null
                    }
                }

                // Check Dimensions
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }

                val boundsStream = openStream()
                if (boundsStream == null) {
                    logger.error("ABORT: Stream was null during bounds check")
                    return@withContext null
                }

                boundsStream.use {
                    BitmapFactory.decodeStream(it, null, options)
                }

                if (options.outWidth == -1 || options.outHeight == -1) {
                    logger.error("ABORT: Failed to decode image bounds")
                    return@withContext null
                }

                // Camera photos store their pixels unrotated and keep the rotation in
                // EXIF. BitmapFactory ignores that tag, so we read and apply it below.
                val orientation = readExifOrientation(openStream())
                val quarterTurned = isQuarterTurned(orientation)

                // Measure the width the user will actually see, not the stored width
                val displayWidth = if (quarterTurned) options.outHeight else options.outWidth

                // Calculate Scale
                options.inSampleSize = calculateInSampleSize(displayWidth)
                options.inJustDecodeBounds = false

                // Decode Bitmap
                val decodeStream = openStream()
                if (decodeStream == null) {
                    logger.error("ABORT: Stream was null during actual decode")
                    return@withContext null
                }

                val decoded = decodeStream.use {
                    BitmapFactory.decodeStream(it, null, options)
                }

                if (decoded == null) {
                    logger.error("ABORT: BitmapFactory returned null bitmap")
                    return@withContext null
                }

                currentCoroutineContext().ensureActive()

                var bitmap: Bitmap = decoded
                try {
                    // Bake the EXIF rotation in, since JPEG compress writes no EXIF
                    bitmap = applyExifOrientation(bitmap, orientation)

                    currentCoroutineContext().ensureActive()

                    // Resize (if needed)
                    if (bitmap.width > MAX_WIDTH) {
                        val aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()
                        val targetHeight = (MAX_WIDTH * aspectRatio).roundToInt()

                        try {
                            val scaledBitmap = bitmap.scale(MAX_WIDTH, targetHeight)
                            if (scaledBitmap != bitmap) {
                                bitmap.recycle()
                                bitmap = scaledBitmap
                            }
                        } catch (e: OutOfMemoryError) {
                            logger.error("OOM during scaling, using original", e)
                        }
                    }

                    currentCoroutineContext().ensureActive()

                    // Compress Loop
                    var quality = 90
                    val stream = ByteArrayOutputStream()

                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

                    while (stream.size() > thresholdBytes && quality > 10) {
                        currentCoroutineContext().ensureActive()
                        stream.reset()
                        quality -= 10
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                    }

                    if (stream.size() > thresholdBytes) {
                        logger.info("Image still above threshold at minimum quality")
                    }

                    val compressedFile =
                        File(context.cacheDir, "compressed_${UUID.randomUUID()}.jpg")
                    FileOutputStream(compressedFile).use { fos ->
                        stream.writeTo(fos)
                    }

                    Uri.fromFile(compressedFile).toString()
                } finally {
                    bitmap.recycle()
                }

            } catch (e: Throwable) {
                currentCoroutineContext().ensureActive()
                logger.error("Image compression failed", e)
                null
            }
        }
    }

    actual suspend fun readBytes(imagePath: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                val cleanPath = imagePath.trim()
                val stream = if (cleanPath.startsWith("file://")) {
                    val file = File(cleanPath.removePrefix("file://"))
                    if (file.exists()) FileInputStream(file) else null
                } else {
                    context.contentResolver.openInputStream(cleanPath.toUri())
                }
                stream?.use { it.readBytes() }
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                null
            }
        }
    }

    // Returns ORIENTATION_NORMAL when the tag is missing or unreadable, so an
    // image without EXIF is simply left alone.
    private fun readExifOrientation(stream: InputStream?): Int {
        if (stream == null) return ExifInterface.ORIENTATION_NORMAL

        return try {
            stream.use {
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }
        } catch (e: Exception) {
            logger.error("Could not read EXIF orientation", e)
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    // These four are the orientations that swap the width and the height
    private fun isQuarterTurned(orientation: Int): Boolean = when (orientation) {
        ExifInterface.ORIENTATION_TRANSPOSE,
        ExifInterface.ORIENTATION_ROTATE_90,
        ExifInterface.ORIENTATION_TRANSVERSE,
        ExifInterface.ORIENTATION_ROTATE_270 -> true

        else -> false
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setRotate(180f)
                matrix.postScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            else -> return bitmap
        }

        return try {
            val oriented = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
            if (oriented != bitmap) {
                bitmap.recycle()
            }
            oriented
        } catch (e: OutOfMemoryError) {
            logger.error("OOM applying EXIF orientation, using original", e)
            bitmap
        }
    }

    // Only the width is capped, so only the width may drive the sample size.
    // Testing the height here would shrink tall images below MAX_WIDTH.
    private fun calculateInSampleSize(width: Int): Int {
        var inSampleSize = 1

        if (width > MAX_WIDTH) {
            val halfWidth = width / 2

            while ((halfWidth / inSampleSize) >= MAX_WIDTH) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}