package com.eeseka.lynk.core.data.hangout

import com.eeseka.lynk.core.data.hangout.dto.HangoutNoteCountDto
import com.eeseka.lynk.core.data.hangout.dto.HangoutNoteDto
import com.eeseka.lynk.core.data.hangout.dto.requests.CreateHangoutNoteRequest
import com.eeseka.lynk.core.data.hangout.dto.requests.UpdateHangoutNoteRequest
import com.eeseka.lynk.core.data.hangout.mappers.toDomain
import com.eeseka.lynk.core.data.networking.delete
import com.eeseka.lynk.core.data.networking.get
import com.eeseka.lynk.core.data.networking.patch
import com.eeseka.lynk.core.data.networking.post
import com.eeseka.lynk.core.domain.hangout.HangoutNoteService
import com.eeseka.lynk.core.domain.hangout.model.HangoutNote
import com.eeseka.lynk.core.domain.hangout.model.HangoutNoteAudience
import com.eeseka.lynk.core.domain.util.DataError
import com.eeseka.lynk.core.domain.util.EmptyResult
import com.eeseka.lynk.core.domain.util.Result
import com.eeseka.lynk.core.domain.util.map
import io.ktor.client.HttpClient

class KtorHangoutNoteService(
    private val httpClient: HttpClient
) : HangoutNoteService {

    override suspend fun getNotes(
        hangoutId: String,
        before: String?
    ): Result<List<HangoutNote>, DataError.Remote> {
        return httpClient.get<List<HangoutNoteDto>>(
            route = "/hangouts/$hangoutId/notes",
            queryParams = buildMap {
                put("pageSize", HangoutConstants.PAGE_SIZE)
                before?.let { put("before", it) }
            }
        ).map { notes ->
            notes.map { it.toDomain() }
        }
    }

    override suspend fun getNoteCount(hangoutId: String): Result<Long, DataError.Remote> {
        return httpClient.get<HangoutNoteCountDto>(
            route = "/hangouts/$hangoutId/notes/count"
        ).map { it.count }
    }

    override suspend fun addNote(
        hangoutId: String,
        content: String,
        audience: HangoutNoteAudience
    ): Result<HangoutNote, DataError.Remote> {
        return httpClient.post<CreateHangoutNoteRequest, HangoutNoteDto>(
            route = "/hangouts/$hangoutId/notes",
            body = CreateHangoutNoteRequest(content = content, audience = audience)
        ).map { it.toDomain() }
    }

    override suspend fun editNote(
        hangoutId: String,
        noteId: String,
        content: String
    ): EmptyResult<DataError.Remote> {
        return httpClient.patch<UpdateHangoutNoteRequest, Unit>(
            route = "/hangouts/$hangoutId/notes/$noteId",
            body = UpdateHangoutNoteRequest(content = content)
        )
    }

    override suspend fun deleteNote(
        hangoutId: String,
        noteId: String
    ): EmptyResult<DataError.Remote> {
        return httpClient.delete<Unit>(
            route = "/hangouts/$hangoutId/notes/$noteId"
        )
    }
}
