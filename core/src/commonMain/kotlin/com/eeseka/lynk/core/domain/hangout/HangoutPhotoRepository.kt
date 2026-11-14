package com.eeseka.lynk.core.domain.hangout

import com.eeseka.lynk.core.domain.util.DataError
import com.eeseka.lynk.core.domain.util.EmptyResult

interface HangoutPhotoRepository {
    suspend fun uploadPhotos(
        hangoutId: String,
        imagePaths: List<String>
    ): EmptyResult<DataError>
}
