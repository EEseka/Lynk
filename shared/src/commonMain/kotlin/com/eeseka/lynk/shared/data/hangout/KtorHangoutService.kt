package com.eeseka.lynk.shared.data.hangout

import com.eeseka.lynk.shared.domain.hangout.HangoutService
import com.eeseka.lynk.shared.domain.hangout.model.Hangout
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result
import io.ktor.client.HttpClient
import kotlin.time.Instant

// TODO: Replace stub bodies with real DTO+HTTP calls when hangout backend endpoints are ready.
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
    ): Result<Hangout, DataError.Remote> = Result.Failure(DataError.Remote.UNKNOWN)

    override suspend fun updateHangout(
        hangoutId: String,
        name: String,
        description: String?,
        vibe: HangoutVibe,
        scheduledAt: Instant,
        maxAttendees: Int?,
        spotId: String?
    ): Result<Hangout, DataError.Remote> = Result.Failure(DataError.Remote.UNKNOWN)

    override suspend fun getHangoutDetails(
        hangoutId: String
    ): Result<Hangout, DataError.Remote> = Result.Failure(DataError.Remote.UNKNOWN)

    override suspend fun getUpcomingHangouts(): Result<List<Hangout>, DataError.Remote> =
        Result.Failure(DataError.Remote.UNKNOWN)

    override suspend fun cancelHangout(hangoutId: String): EmptyResult<DataError.Remote> =
        Result.Failure(DataError.Remote.UNKNOWN)
}
