package com.eeseka.lynk.shared.data.hangout

import com.eeseka.lynk.shared.data.hangout.dto.HangoutDto
import com.eeseka.lynk.shared.data.hangout.dto.HangoutParticipantDto
import com.eeseka.lynk.shared.data.hangout.dto.HangoutPreviewDto
import com.eeseka.lynk.shared.data.hangout.dto.HangoutSummaryDto
import com.eeseka.lynk.shared.data.hangout.dto.requests.CreateHangoutRequest
import com.eeseka.lynk.shared.data.hangout.dto.requests.InviteParticipantRequest
import com.eeseka.lynk.shared.data.hangout.dto.requests.RsvpRequest
import com.eeseka.lynk.shared.data.hangout.dto.requests.UpdateHangoutRequest
import com.eeseka.lynk.shared.data.hangout.mappers.toDomain
import com.eeseka.lynk.shared.data.networking.delete
import com.eeseka.lynk.shared.data.networking.get
import com.eeseka.lynk.shared.data.networking.patch
import com.eeseka.lynk.shared.data.networking.post
import com.eeseka.lynk.shared.data.networking.put
import com.eeseka.lynk.shared.domain.hangout.HangoutService
import com.eeseka.lynk.shared.domain.hangout.model.Hangout
import com.eeseka.lynk.shared.domain.hangout.model.HangoutParticipant
import com.eeseka.lynk.shared.domain.hangout.model.HangoutPreview
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutSummary
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result
import com.eeseka.lynk.shared.domain.util.map
import io.ktor.client.HttpClient
import kotlin.time.Instant

class KtorHangoutService(
    private val httpClient: HttpClient
) : HangoutService {

    override suspend fun createHangout(
        name: String,
        description: String?,
        vibe: HangoutVibe,
        scheduledAt: Instant,
        maxAttendees: Int?,
        spotId: String?
    ): Result<Hangout, DataError.Remote> {
        return httpClient.post<CreateHangoutRequest, HangoutDto>(
            route = "/hangouts",
            body = CreateHangoutRequest(
                name = name,
                description = description,
                vibe = vibe,
                scheduledAt = scheduledAt,
                maxAttendees = maxAttendees,
                spotId = spotId
            )
        ).map { it.toDomain() }
    }

    override suspend fun updateHangout(
        hangoutId: String,
        name: String,
        description: String?,
        vibe: HangoutVibe,
        scheduledAt: Instant,
        maxAttendees: Int?,
        spotId: String?
    ): Result<Hangout, DataError.Remote> {
        return httpClient.put<UpdateHangoutRequest, HangoutDto>(
            route = "/hangouts/$hangoutId",
            body = UpdateHangoutRequest(
                name = name,
                description = description,
                vibe = vibe,
                scheduledAt = scheduledAt,
                maxAttendees = maxAttendees,
                spotId = spotId
            )
        ).map { it.toDomain() }
    }

    override suspend fun getHangoutDetails(hangoutId: String): Result<Hangout, DataError.Remote> {
        return httpClient.get<HangoutDto>(
            route = "/hangouts/$hangoutId"
        ).map { it.toDomain() }
    }

    override suspend fun getHangoutPreview(hangoutId: String): Result<HangoutPreview, DataError.Remote> {
        return httpClient.get<HangoutPreviewDto>(
            route = "/hangouts/$hangoutId/preview"
        ).map { it.toDomain() }
    }

    override suspend fun getHangouts(
        query: String?,
        status: HangoutStatus?,
        vibe: HangoutVibe?,
        before: String?
    ): Result<List<HangoutSummary>, DataError.Remote> {
        return httpClient.get<List<HangoutSummaryDto>>(
            route = "/hangouts",
            queryParams = buildMap {
                put("pageSize", HangoutConstants.PAGE_SIZE)
                query?.let { put("query", it) }
                status?.let { put("status", it) }
                vibe?.let { put("vibe", it) }
                before?.let { put("before", it) }
            }
        ).map { hangoutSummaries ->
            hangoutSummaries.map { it.toDomain() }
        }
    }

    override suspend fun cancelHangout(hangoutId: String): EmptyResult<DataError.Remote> {
        return httpClient.delete<Unit>(
            route = "/hangouts/$hangoutId"
        )
    }

    override suspend fun completeHangout(hangoutId: String): EmptyResult<DataError.Remote> {
        return httpClient.patch<Unit>(
            route = "/hangouts/$hangoutId/complete"
        )
    }

    override suspend fun inviteParticipant(
        hangoutId: String,
        userId: String
    ): Result<HangoutParticipant, DataError.Remote> {
        return httpClient.post<InviteParticipantRequest, HangoutParticipantDto>(
            route = "/hangouts/$hangoutId/participants",
            body = InviteParticipantRequest(userId = userId)
        ).map { it.toDomain() }
    }

    override suspend fun updateRsvp(
        hangoutId: String,
        rsvpStatus: RsvpStatus
    ): Result<HangoutParticipant, DataError.Remote> {
        return httpClient.patch<RsvpRequest, HangoutParticipantDto>(
            route = "/hangouts/$hangoutId/rsvp",
            body = RsvpRequest(rsvpStatus = rsvpStatus)
        ).map { it.toDomain() }
    }

    override suspend fun removeParticipant(
        hangoutId: String,
        userId: String
    ): EmptyResult<DataError.Remote> {
        return httpClient.delete<Unit>(
            route = "/hangouts/$hangoutId/participants/$userId"
        )
    }

    override suspend fun leaveHangout(hangoutId: String): EmptyResult<DataError.Remote> {
        return httpClient.delete<Unit>(
            route = "/hangouts/$hangoutId/leave"
        )
    }
}