package com.eeseka.lynk.testing.data

import com.eeseka.lynk.core.domain.hangout.HangoutPhotoRepository
import com.eeseka.lynk.core.domain.util.DataError
import com.eeseka.lynk.core.domain.util.EmptyResult
import com.eeseka.lynk.core.domain.util.Result

class FakeHangoutPhotoRepository : HangoutPhotoRepository {
    var shouldReturnError = false
    var errorToReturn: DataError = DataError.Remote.SERVER_ERROR

    val uploadedImagePaths = mutableListOf<String>()

    override suspend fun uploadPhotos(
        hangoutId: String,
        imagePaths: List<String>
    ): EmptyResult<DataError> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        uploadedImagePaths.addAll(imagePaths)
        return Result.Success(Unit)
    }
}
