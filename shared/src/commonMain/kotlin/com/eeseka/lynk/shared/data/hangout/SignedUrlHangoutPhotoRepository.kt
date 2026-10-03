package com.eeseka.lynk.shared.data.hangout

import com.eeseka.lynk.shared.domain.hangout.HangoutPhotoRepository
import com.eeseka.lynk.shared.domain.hangout.HangoutPhotoService
import com.eeseka.lynk.shared.domain.hangout.model.HangoutPhotoUploadUrls
import com.eeseka.lynk.shared.domain.media.ImageCompressionService
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result
import com.eeseka.lynk.shared.domain.util.onFailure
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class SignedUrlHangoutPhotoRepository(
    private val hangoutPhotoService: HangoutPhotoService,
    private val imageCompressionService: ImageCompressionService
) : HangoutPhotoRepository {

    private companion object {
        const val FULL_MAX_WIDTH = 1600
        const val FULL_THRESHOLD_BYTES = 1024 * 1024L
        const val THUMBNAIL_MAX_WIDTH = 480
        const val THUMBNAIL_THRESHOLD_BYTES = 60 * 1024L

        // Each compression holds a decoded bitmap, so more at once risks running out of memory
        const val MAX_PHOTOS_IN_FLIGHT = 3
    }

    override suspend fun uploadPhotos(
        hangoutId: String,
        imagePaths: List<String>
    ): EmptyResult<DataError> {
        val semaphore = Semaphore(MAX_PHOTOS_IN_FLIGHT)

        val compressedPhotos = coroutineScope {
            imagePaths.map { imagePath ->
                async { semaphore.withPermit { compressPhoto(imagePath) } }
            }.awaitAll().filterNotNull()
        }
        if (compressedPhotos.isEmpty()) return Result.Failure(DataError.Local.UNKNOWN)

        val uploadUrlsResult = hangoutPhotoService.generateUploadUrls(
            hangoutId = hangoutId,
            count = compressedPhotos.size
        )
        val uploadUrls = when (uploadUrlsResult) {
            is Result.Failure -> return uploadUrlsResult
            is Result.Success -> uploadUrlsResult.data
        }

        val uploadResults = coroutineScope {
            compressedPhotos.zip(uploadUrls).map { (photoBytes, urls) ->
                async {
                    semaphore.withPermit {
                        uploadPhoto(hangoutId = hangoutId, photoBytes = photoBytes, urls = urls)
                            .onFailure {
                                // Gives the slot back; if this fails too, the server clears it after two hours
                                hangoutPhotoService.deletePhoto(hangoutId = hangoutId, photoId = urls.photoId)
                            }
                    }
                }
            }.awaitAll()
        }

        val uploadError = uploadResults.firstNotNullOfOrNull { result -> (result as? Result.Failure)?.error }
        val compressionError = if (compressedPhotos.size < imagePaths.size) DataError.Local.UNKNOWN else null
        val failedPhotoError = uploadError ?: compressionError

        return failedPhotoError?.let { Result.Failure(it) } ?: Result.Success(Unit)
    }

    private suspend fun uploadPhoto(
        hangoutId: String,
        photoBytes: Pair<ByteArray, ByteArray>,
        urls: HangoutPhotoUploadUrls
    ): EmptyResult<DataError.Remote> {
        val (fullBytes, thumbnailBytes) = photoBytes

        val fullResult = hangoutPhotoService.uploadPhotoFile(
            uploadUrl = urls.fullUploadUrl,
            headers = urls.headers,
            imageBytes = fullBytes
        )
        if (fullResult is Result.Failure) return fullResult

        val thumbnailResult = hangoutPhotoService.uploadPhotoFile(
            uploadUrl = urls.thumbnailUploadUrl,
            headers = urls.headers,
            imageBytes = thumbnailBytes
        )
        if (thumbnailResult is Result.Failure) return thumbnailResult

        return hangoutPhotoService.confirmUpload(hangoutId = hangoutId, photoId = urls.photoId)
    }

    private suspend fun compressPhoto(imagePath: String): Pair<ByteArray, ByteArray>? {
        val fullPath = imageCompressionService.compress(
            contentPath = imagePath,
            maxWidth = FULL_MAX_WIDTH,
            thresholdBytes = FULL_THRESHOLD_BYTES
        )
        val thumbnailPath = imageCompressionService.compress(
            contentPath = imagePath,
            maxWidth = THUMBNAIL_MAX_WIDTH,
            thresholdBytes = THUMBNAIL_THRESHOLD_BYTES
        )
        if (fullPath == null || thumbnailPath == null) return null

        val fullBytes = imageCompressionService.readBytes(fullPath) ?: return null
        val thumbnailBytes = imageCompressionService.readBytes(thumbnailPath) ?: return null
        return fullBytes to thumbnailBytes
    }
}
