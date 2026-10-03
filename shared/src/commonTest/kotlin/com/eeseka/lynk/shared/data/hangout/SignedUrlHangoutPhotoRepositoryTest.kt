package com.eeseka.lynk.shared.data.hangout

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.Result
import com.eeseka.lynk.testing.data.FakeHangoutPhotoService
import com.eeseka.lynk.testing.data.FakeImageCompressionService
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class SignedUrlHangoutPhotoRepositoryTest {

    private lateinit var hangoutPhotoService: FakeHangoutPhotoService
    private lateinit var imageCompressionService: FakeImageCompressionService
    private lateinit var repository: SignedUrlHangoutPhotoRepository

    @BeforeTest
    fun setUp() {
        hangoutPhotoService = FakeHangoutPhotoService()
        imageCompressionService = FakeImageCompressionService()
        repository = SignedUrlHangoutPhotoRepository(hangoutPhotoService, imageCompressionService)
    }

    @Test
    fun `uploads the full image and the thumbnail of each photo and then confirms it`() = runTest {
        val result = repository.uploadPhotos(HANGOUT_ID, listOf("first.jpg", "second.jpg"))

        assertThat(result).isEqualTo(Result.Success(Unit))
        assertThat(hangoutPhotoService.uploadedFileUrls).containsExactly(
            "https://storage.test/photo-1/full",
            "https://storage.test/photo-1/thumb",
            "https://storage.test/photo-2/full",
            "https://storage.test/photo-2/thumb"
        )
        assertThat(hangoutPhotoService.confirmedPhotoIds).containsExactly("photo-1", "photo-2")
    }

    @Test
    fun `uploads three photos at a time`() = runTest {
        hangoutPhotoService.uploadResponseDelay = 1.seconds
        val imagePaths = List(10) { number -> "photo_$number.jpg" }

        val result = repository.uploadPhotos(HANGOUT_ID, imagePaths)

        assertThat(result).isEqualTo(Result.Success(Unit))
        assertThat(hangoutPhotoService.maxUploadsInFlight).isEqualTo(3)
        assertThat(hangoutPhotoService.confirmedPhotoIds).hasSize(10)
    }

    @Test
    fun `gives back the slot of a photo that failed to upload and still adds the others`() = runTest {
        hangoutPhotoService.failingUploadUrl = "https://storage.test/photo-1/full"

        val result = repository.uploadPhotos(HANGOUT_ID, listOf("first.jpg", "second.jpg"))

        assertThat(result).isEqualTo(Result.Failure(DataError.Remote.REQUEST_TIMEOUT))
        assertThat(hangoutPhotoService.deletedPhotoIds).containsExactly("photo-1")
        assertThat(hangoutPhotoService.confirmedPhotoIds).containsExactly("photo-2")
    }

    @Test
    fun `asks for no upload urls when no photo could be compressed`() = runTest {
        imageCompressionService.shouldFailCompress = true

        val result = repository.uploadPhotos(HANGOUT_ID, listOf("first.jpg"))

        assertThat(result).isEqualTo(Result.Failure(DataError.Local.UNKNOWN))
        assertThat(hangoutPhotoService.uploadedFileUrls).isEmpty()
    }

    @Test
    fun `passes on the server refusing the upload`() = runTest {
        hangoutPhotoService.shouldReturnError = true
        hangoutPhotoService.errorToReturn = DataError.Remote.CONFLICT

        val result = repository.uploadPhotos(HANGOUT_ID, listOf("first.jpg"))

        assertThat(result).isEqualTo(Result.Failure(DataError.Remote.CONFLICT))
        assertThat(hangoutPhotoService.uploadedFileUrls).isEmpty()
    }

    private companion object {
        const val HANGOUT_ID = "hangout-1"
    }
}
