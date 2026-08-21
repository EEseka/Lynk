package com.eeseka.lynk.shared.domain.hangout

import com.eeseka.lynk.shared.domain.hangout.model.Hangout
import com.eeseka.lynk.shared.domain.hangout.model.HangoutParticipant
import com.eeseka.lynk.shared.domain.hangout.model.HangoutPreview
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStats
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutSummary
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
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

    /**
     * The glimpse a PENDING invitee sees before accepting — lighter than full details,
     * attendees are ATTENDING-only social proof.
     */
    suspend fun getHangoutPreview(hangoutId: String): Result<HangoutPreview, DataError.Remote>

    suspend fun getHangouts(
        query: String? = null,
        status: HangoutStatus? = null,
        vibe: HangoutVibe? = null,
        before: String? = null
    ): Result<List<HangoutSummary>, DataError.Remote>

    suspend fun getMyStats(): Result<HangoutStats, DataError.Remote>

    suspend fun cancelHangout(hangoutId: String): EmptyResult<DataError.Remote>

    suspend fun completeHangout(hangoutId: String): EmptyResult<DataError.Remote>

    suspend fun inviteParticipant(
        hangoutId: String,
        userId: String
    ): Result<HangoutParticipant, DataError.Remote>

    suspend fun updateRsvp(
        hangoutId: String,
        rsvpStatus: RsvpStatus
    ): Result<HangoutParticipant, DataError.Remote>

    suspend fun removeParticipant(
        hangoutId: String,
        userId: String
    ): EmptyResult<DataError.Remote>

    suspend fun leaveHangout(hangoutId: String): EmptyResult<DataError.Remote>
}