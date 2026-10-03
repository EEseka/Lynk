package com.eeseka.lynk.shared.domain.hangout

import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult

interface HangoutPhotoRepository {
    suspend fun uploadPhotos(
        hangoutId: String,
        imagePaths: List<String>
    ): EmptyResult<DataError>
}
