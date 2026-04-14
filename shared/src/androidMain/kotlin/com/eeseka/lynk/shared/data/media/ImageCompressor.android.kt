package com.eeseka.lynk.shared.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.scale
import androidx.core.net.toUri
import com.eeseka.lynk.shared.domain.logging.LynkLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
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

                // Calculate Scale
                options.inSampleSize = calculateInSampleSize(options)
                options.inJustDecodeBounds = false

                // Decode Bitmap
                val decodeStream = openStream()
                if (decodeStream == null) {
                    logger.error("ABORT: Stream was null during actual decode")
                    return@withContext null
                }

                var bitmap = decodeStream.use {
                    BitmapFactory.decodeStream(it, null, options)
                }

                if (bitmap == null) {
                    logger.error("ABORT: BitmapFactory returned null bitmap")
                    return@withContext null
                }

                ensureActive()

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

                ensureActive()

                // Compress Loop
                var quality = 90
                val stream = ByteArrayOutputStream()

                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

                while (stream.size() > thresholdBytes && quality > 10) {
                    ensureActive()
                    stream.reset()
                    quality -= 10
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                }

                val compressedFile =
                    File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
                FileOutputStream(compressedFile).use { fos ->
                    stream.writeTo(fos)
                }

                bitmap.recycle()

                val resultPath = Uri.fromFile(compressedFile).toString()

                resultPath

            } catch (e: Throwable) {
                logger.error("UNHANDLED CRASH", e)
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
                null
            }
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > MAX_WIDTH || width > MAX_WIDTH) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= MAX_WIDTH || (halfWidth / inSampleSize) >= MAX_WIDTH) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}