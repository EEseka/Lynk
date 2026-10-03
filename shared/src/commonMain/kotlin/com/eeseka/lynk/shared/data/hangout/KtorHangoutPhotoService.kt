package com.eeseka.lynk.shared.data.hangout

import com.eeseka.lynk.shared.data.hangout.dto.HangoutPhotoDto
import com.eeseka.lynk.shared.data.hangout.dto.HangoutPhotoStatsDto
import com.eeseka.lynk.shared.data.hangout.dto.requests.GenerateHangoutPhotoUploadUrlsRequest
import com.eeseka.lynk.shared.data.hangout.dto.requests.UpdateHangoutPhotoCaptionRequest
import com.eeseka.lynk.shared.data.hangout.dto.response.HangoutPhotoUploadUrlsResponse
import com.eeseka.lynk.shared.data.hangout.mappers.toDomain
import com.eeseka.lynk.shared.data.networking.delete
import com.eeseka.lynk.shared.data.networking.get
import com.eeseka.lynk.shared.data.networking.patch
import com.eeseka.lynk.shared.data.networking.post
import com.eeseka.lynk.shared.data.networking.safeCall
import com.eeseka.lynk.shared.domain.hangout.HangoutPhotoService
import com.eeseka.lynk.shared.domain.hangout.model.HangoutPhoto
import com.eeseka.lynk.shared.domain.hangout.model.HangoutPhotoStats
import com.eeseka.lynk.shared.domain.hangout.model.HangoutPhotoUploadUrls
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result
import com.eeseka.lynk.shared.domain.util.map
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url

class KtorHangoutPhotoService(
    private val httpClient: HttpClient
) : HangoutPhotoService {

    override suspend fun getPhotos(
        hangoutId: String,
        before: String?
    ): Result<List<HangoutPhoto>, DataError.Remote> {
        return httpClient.get<List<HangoutPhotoDto>>(
            route = "/hangouts/$hangoutId/photos",
            queryParams = buildMap {
                put("pageSize", HangoutConstants.PAGE_SIZE)
                before?.let { put("before", it) }
            }
        ).map { photos ->
            photos.map { it.toDomain() }
        }
    }

    override suspend fun getPhotoStats(hangoutId: String): Result<HangoutPhotoStats, DataError.Remote> {
        return httpClient.get<HangoutPhotoStatsDto>(
            route = "/hangouts/$hangoutId/photos/stats"
        ).map { it.toDomain() }
    }

    override suspend fun generateUploadUrls(
        hangoutId: String,
        count: Int
    ): Result<List<HangoutPhotoUploadUrls>, DataError.Remote> {
        return httpClient.post<GenerateHangoutPhotoUploadUrlsRequest, List<HangoutPhotoUploadUrlsResponse>>(
            route = "/hangouts/$hangoutId/photos/generate-upload-urls",
            body = GenerateHangoutPhotoUploadUrlsRequest(count = count)
        ).map { uploads ->
            uploads.map { it.toDomain() }
        }
    }

    override suspend fun uploadPhotoFile(
        uploadUrl: String,
        headers: Map<String, String>,
        imageBytes: ByteArray
    ): EmptyResult<DataError.Remote> {
        return safeCall {
            httpClient.put {
                url(uploadUrl)
                headers.forEach { (key, value) ->
                    header(key, value)
                }
                setBody(imageBytes)
            }
        }
    }

    override suspend fun confirmUpload(
        hangoutId: String,
        photoId: String
    ): EmptyResult<DataError.Remote> {
        return httpClient.post<Unit>(
            route = "/hangouts/$hangoutId/photos/$photoId/confirm"
        )
    }

    override suspend fun updateCaption(
        hangoutId: String,
        photoId: String,
        caption: String?
    ): EmptyResult<DataError.Remote> {
        return httpClient.patch<UpdateHangoutPhotoCaptionRequest, Unit>(
            route = "/hangouts/$hangoutId/photos/$photoId",
            body = UpdateHangoutPhotoCaptionRequest(caption = caption)
        )
    }

    override suspend fun deletePhoto(
        hangoutId: String,
        photoId: String
    ): EmptyResult<DataError.Remote> {
        return httpClient.delete<Unit>(
            route = "/hangouts/$hangoutId/photos/$photoId"
        )
    }
}
