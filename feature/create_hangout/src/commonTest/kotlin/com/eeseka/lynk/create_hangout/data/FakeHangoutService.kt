package com.eeseka.lynk.create_hangout.data

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
import kotlin.time.Instant

class FakeHangoutService : HangoutService {
    var shouldReturnError = false
    var lastCreatedSpotId: String? = "NOT_SET"
    var lastUpdatedSpotId: String? = "NOT_SET"

    private val now = Instant.fromEpochMilliseconds(4102444800000) // 2100-01-01

    private fun dummyHangout(name: String, spotId: String?) = Hangout(
        id = "hangout_1",
        hostId = "host_1",
        name = name,
        description = null,
        vibe = HangoutVibe.CHILL,
        status = if (spotId != null) HangoutStatus.SCHEDULED else HangoutStatus.VOTING,
        scheduledAt = now,
        maxAttendees = null,
        participantCount = 1,
        chosenSpot = null,
        participants = emptyList(),
        payment = null,
        createdAt = now
    )

    override suspend fun createHangout(
        name: String,
        description: String?,
        vibe: HangoutVibe,
        scheduledAt: Instant,
        maxAttendees: Int?,
        spotId: String?
    ): Result<Hangout, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(DataError.Remote.SERVER_ERROR)
        lastCreatedSpotId = spotId
        return Result.Success(dummyHangout(name, spotId))
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
        if (shouldReturnError) return Result.Failure(DataError.Remote.SERVER_ERROR)
        lastUpdatedSpotId = spotId
        return Result.Success(dummyHangout(name, spotId))
    }

    override suspend fun getHangoutDetails(hangoutId: String): Result<Hangout, DataError.Remote> =
        Result.Failure(DataError.Remote.SERVER_ERROR)

    override suspend fun getHangouts(
        query: String?,
        status: HangoutStatus?,
        vibe: HangoutVibe?,
        before: String?
    ): Result<List<HangoutSummary>, DataError.Remote> = Result.Failure(DataError.Remote.SERVER_ERROR)

    override suspend fun cancelHangout(hangoutId: String): EmptyResult<DataError.Remote> =
        Result.Failure(DataError.Remote.SERVER_ERROR)

    override suspend fun completeHangout(hangoutId: String): EmptyResult<DataError.Remote> =
        Result.Failure(DataError.Remote.SERVER_ERROR)

    override suspend fun getHangoutPreview(hangoutId: String): Result<HangoutPreview, DataError.Remote> =
        Result.Failure(DataError.Remote.SERVER_ERROR)

    override suspend fun inviteParticipant(
        hangoutId: String,
        userId: String
    ): Result<HangoutParticipant, DataError.Remote> = Result.Failure(DataError.Remote.SERVER_ERROR)

    override suspend fun updateRsvp(
        hangoutId: String,
        rsvpStatus: RsvpStatus
    ): Result<HangoutParticipant, DataError.Remote> = Result.Failure(DataError.Remote.SERVER_ERROR)

    override suspend fun removeParticipant(
        hangoutId: String,
        userId: String
    ): EmptyResult<DataError.Remote> = Result.Failure(DataError.Remote.SERVER_ERROR)

    override suspend fun leaveHangout(hangoutId: String): EmptyResult<DataError.Remote> =
        Result.Failure(DataError.Remote.SERVER_ERROR)
}