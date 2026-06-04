package com.eeseka.lynk.shared.domain.hangout

import com.eeseka.lynk.shared.domain.hangout.model.Hangout
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutSummary
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result
import kotlin.time.Instant

interface HangoutService {

    /**
     * Creates a new hangout.
     * If spotId is provided (The Dictator), the hangout is SCHEDULED.
     * If spotId is null (The Democracy), the hangout is VOTING.
     */
    suspend fun createHangout(
        name: String,
        description: String?,
        vibe: HangoutVibe,
        scheduledAt: Instant,
        maxAttendees: Int?,
        spotId: String?
    ): Result<Hangout, DataError.Remote>

    /**
     * Updates the details of a hangout.
     * The backend will dynamically adjust the HangoutStatus based on the spotId provided.
     */
    suspend fun updateHangout(
        hangoutId: String,
        name: String,
        description: String?,
        vibe: HangoutVibe,
        scheduledAt: Instant,
        maxAttendees: Int?,
        spotId: String?
    ): Result<Hangout, DataError.Remote>

    /**
     * Fetches details for a specific hangout.
     * Crucial for deep links or when entering the lobby from a push notification.
     */
    suspend fun getHangoutDetails(hangoutId: String): Result<Hangout, DataError.Remote>

    suspend fun getHangouts(
        query: String? = null,
        status: HangoutStatus? = null,
        vibe: HangoutVibe? = null,
        before: String? = null
    ): Result<List<HangoutSummary>, DataError.Remote>

    suspend fun cancelHangout(hangoutId: String): EmptyResult<DataError.Remote>

    suspend fun completeHangout(hangoutId: String): EmptyResult<DataError.Remote>
}