package com.eeseka.lynk.testing.data

import com.eeseka.lynk.shared.domain.hangout.HangoutPhotoService
import com.eeseka.lynk.shared.domain.hangout.model.HangoutPhoto
import com.eeseka.lynk.shared.domain.hangout.model.HangoutPhotoStats
import com.eeseka.lynk.shared.domain.hangout.model.HangoutPhotoUploadUrls
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Instant

class FakeHangoutPhotoService : HangoutPhotoService {
    var shouldReturnError = false
    var errorToReturn = DataError.Remote.SERVER_ERROR
    var photos = mutableListOf<HangoutPhoto>()
    var myPhotoCount = 0

    // Holds the counts back, like a slow network, so a test can look before they land
    var photosResponseDelay = Duration.ZERO
    var statsResponseDelay = Duration.ZERO
    var deleteResponseDelay = Duration.ZERO
    var uploadResponseDelay = Duration.ZERO

    // The most file uploads that were running at the same moment
    var maxUploadsInFlight = 0
    private var uploadsInFlight = 0

    // Only the upload to this url fails, like one file dropping out of a batch
    var failingUploadUrl: String? = null

    val uploadedFileUrls = mutableListOf<String>()
    val confirmedPhotoIds = mutableListOf<String>()
    val deletedPhotoIds = mutableListOf<String>()

    private var nextPhotoNumber = 1

    override suspend fun getPhotos(
        hangoutId: String,
        before: String?
    ): Result<List<HangoutPhoto>, DataError.Remote> {
        delay(photosResponseDelay)
        if (shouldReturnError) return Result.Failure(errorToReturn)

        val page = photos.filter { photo ->
            before == null || photo.createdAt < Instant.parse(before)
        }
        return Result.Success(page)
    }

    override suspend fun getPhotoStats(hangoutId: String): Result<HangoutPhotoStats, DataError.Remote> {
        delay(statsResponseDelay)
        if (shouldReturnError) return Result.Failure(errorToReturn)

        return Result.Success(
            HangoutPhotoStats(
                photoCount = photos.size,
                myPhotoCount = myPhotoCount
            )
        )
    }

    override suspend fun generateUploadUrls(
        hangoutId: String,
        count: Int
    ): Result<List<HangoutPhotoUploadUrls>, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        val uploads = List(count) {
            val photoId = "photo-${nextPhotoNumber++}"
            HangoutPhotoUploadUrls(
                photoId = photoId,
                fullUploadUrl = "https://storage.test/$photoId/full",
                thumbnailUploadUrl = "https://storage.test/$photoId/thumb",
                headers = emptyMap()
            )
        }
        return Result.Success(uploads)
    }

    override suspend fun uploadPhotoFile(
        uploadUrl: String,
        headers: Map<String, String>,
        imageBytes: ByteArray
    ): EmptyResult<DataError.Remote> {
        uploadsInFlight++
        maxUploadsInFlight = maxOf(maxUploadsInFlight, uploadsInFlight)
        delay(uploadResponseDelay)
        uploadsInFlight--

        if (shouldReturnError) return Result.Failure(errorToReturn)
        if (uploadUrl == failingUploadUrl) return Result.Failure(DataError.Remote.REQUEST_TIMEOUT)

        uploadedFileUrls.add(uploadUrl)
        return Result.Success(Unit)
    }

    override suspend fun confirmUpload(
        hangoutId: String,
        photoId: String
    ): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        confirmedPhotoIds.add(photoId)
        return Result.Success(Unit)
    }

    override suspend fun updateCaption(
        hangoutId: String,
        photoId: String,
        caption: String?
    ): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        val index = photos.indexOfFirst { it.id == photoId }
        if (index == -1) return Result.Failure(DataError.Remote.NOT_FOUND)
        photos[index] = photos[index].copy(caption = caption)
        return Result.Success(Unit)
    }

    override suspend fun deletePhoto(
        hangoutId: String,
        photoId: String
    ): EmptyResult<DataError.Remote> {
        delay(deleteResponseDelay)
        if (shouldReturnError) return Result.Failure(errorToReturn)
        // Like the server, a photo that was already deleted is not found
        if (photoId in deletedPhotoIds) return Result.Failure(DataError.Remote.NOT_FOUND)

        deletedPhotoIds.add(photoId)
        photos.removeAll { it.id == photoId }
        return Result.Success(Unit)
    }
}
