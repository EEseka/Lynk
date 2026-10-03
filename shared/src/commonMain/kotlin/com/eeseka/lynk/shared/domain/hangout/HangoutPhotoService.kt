package com.eeseka.lynk.shared.domain.hangout

import com.eeseka.lynk.shared.domain.hangout.model.HangoutPhoto
import com.eeseka.lynk.shared.domain.hangout.model.HangoutPhotoStats
import com.eeseka.lynk.shared.domain.hangout.model.HangoutPhotoUploadUrls
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result

interface HangoutPhotoService {
    suspend fun getPhotos(
        hangoutId: String,
        before: String?
    ): Result<List<HangoutPhoto>, DataError.Remote>

    suspend fun getPhotoStats(hangoutId: String): Result<HangoutPhotoStats, DataError.Remote>

    suspend fun generateUploadUrls(
        hangoutId: String,
        count: Int
    ): Result<List<HangoutPhotoUploadUrls>, DataError.Remote>

    suspend fun uploadPhotoFile(
        uploadUrl: String,
        headers: Map<String, String>,
        imageBytes: ByteArray
    ): EmptyResult<DataError.Remote>

    suspend fun confirmUpload(
        hangoutId: String,
        photoId: String
    ): EmptyResult<DataError.Remote>

    suspend fun updateCaption(
        hangoutId: String,
        photoId: String,
        caption: String?
    ): EmptyResult<DataError.Remote>

    suspend fun deletePhoto(
        hangoutId: String,
        photoId: String
    ): EmptyResult<DataError.Remote>
}
