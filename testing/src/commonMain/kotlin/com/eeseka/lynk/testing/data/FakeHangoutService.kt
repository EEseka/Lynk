package com.eeseka.lynk.testing.data

import com.eeseka.lynk.shared.domain.hangout.HangoutService
import com.eeseka.lynk.shared.domain.hangout.model.Hangout
import com.eeseka.lynk.shared.domain.hangout.model.HangoutParticipant
import com.eeseka.lynk.shared.domain.hangout.model.HangoutPreview
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStats
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutSummary
import com.eeseka.lynk.shared.domain.hangout.model.HangoutUser
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result
import kotlin.time.Instant

class FakeHangoutService : HangoutService {
    var shouldReturnError = false
    var errorToReturn = DataError.Remote.SERVER_ERROR

    // Fails only the RSVP call, so a test can refuse an answer while the hangout still reads fine.
    var rsvpErrorToReturn: DataError.Remote? = null
    var currentUserId = "user_1"
    var hangouts = mutableListOf<Hangout>()
    var statsToReturn = HangoutStats(hostedCount = 0, attendedCount = 0)

    // "NOT_SET" until a call arrives, so a test can tell a null spotId apart from no call at all
    var lastCreatedSpotId: String? = "NOT_SET"
    var lastUpdatedSpotId: String? = "NOT_SET"

    private val now = Instant.fromEpochMilliseconds(4102444800000) // 2100-01-01

    override suspend fun createHangout(
        name: String,
        description: String?,
        vibe: HangoutVibe,
        scheduledAt: Instant,
        maxAttendees: Int?,
        spotId: String?
    ): Result<Hangout, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        lastCreatedSpotId = spotId

        val host = HangoutParticipant(
            user = hangoutUser(currentUserId),
            rsvpStatus = RsvpStatus.ATTENDING,
            hasPaid = false
        )
        val hangout = Hangout(
            id = "hangout_${hangouts.size + 1}",
            hostId = currentUserId,
            name = name,
            description = description,
            vibe = vibe,
            status = if (spotId != null) HangoutStatus.SCHEDULED else HangoutStatus.VOTING,
            scheduledAt = scheduledAt,
            maxAttendees = maxAttendees,
            participantCount = 1,
            chosenSpot = null,
            participants = listOf(host),
            payment = null,
            createdAt = now
        )
        hangouts.add(hangout)
        return Result.Success(hangout)
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
        if (shouldReturnError) return Result.Failure(errorToReturn)
        lastUpdatedSpotId = spotId

        val updated = changeHangout(hangoutId) {
            it.copy(
                name = name,
                description = description,
                vibe = vibe,
                scheduledAt = scheduledAt,
                maxAttendees = maxAttendees
            )
        } ?: return Result.Failure(DataError.Remote.NOT_FOUND)
        return Result.Success(updated)
    }

    override suspend fun getHangoutDetails(hangoutId: String): Result<Hangout, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        val hangout = hangouts.find { it.id == hangoutId }
            ?: return Result.Failure(DataError.Remote.NOT_FOUND)
        return Result.Success(hangout)
    }

    override suspend fun getHangoutPreview(hangoutId: String): Result<HangoutPreview, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        val hangout = hangouts.find { it.id == hangoutId }
            ?: return Result.Failure(DataError.Remote.NOT_FOUND)
        return Result.Success(
            HangoutPreview(
                id = hangout.id,
                hostId = hangout.hostId,
                name = hangout.name,
                description = hangout.description,
                vibe = hangout.vibe,
                status = hangout.status,
                scheduledAt = hangout.scheduledAt,
                maxAttendees = hangout.maxAttendees,
                participantCount = hangout.participantCount,
                chosenSpot = hangout.chosenSpot,
                attendees = hangout.participants
                    .filter { it.rsvpStatus == RsvpStatus.ATTENDING }
                    .map { it.user },
                createdAt = hangout.createdAt
            )
        )
    }

    // Like the backend, "before" is a createdAt cursor: the next page holds only older hangouts
    override suspend fun getHangouts(
        query: String?,
        status: HangoutStatus?,
        vibe: HangoutVibe?,
        before: String?
    ): Result<List<HangoutSummary>, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        val summaries = hangouts
            .filter { hangout ->
                val matchesQuery = query.isNullOrBlank() || hangout.name.contains(query, ignoreCase = true)
                val matchesStatus = status == null || hangout.status == status
                val matchesVibe = vibe == null || hangout.vibe == vibe
                val isOnThisPage = before == null || hangout.createdAt < Instant.parse(before)
                matchesQuery && matchesStatus && matchesVibe && isOnThisPage
            }
            .map { hangout ->
                HangoutSummary(
                    id = hangout.id,
                    hostId = hangout.hostId,
                    name = hangout.name,
                    vibe = hangout.vibe,
                    status = hangout.status,
                    scheduledAt = hangout.scheduledAt,
                    maxAttendees = hangout.maxAttendees,
                    participantCount = hangout.participantCount,
                    createdAt = hangout.createdAt
                )
            }
        return Result.Success(summaries)
    }

    override suspend fun getMyStats(): Result<HangoutStats, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        return Result.Success(statsToReturn)
    }

    override suspend fun cancelHangout(hangoutId: String): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        changeHangout(hangoutId) { it.copy(status = HangoutStatus.CANCELLED) }
            ?: return Result.Failure(DataError.Remote.NOT_FOUND)
        return Result.Success(Unit)
    }

    override suspend fun completeHangout(hangoutId: String): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        changeHangout(hangoutId) { it.copy(status = HangoutStatus.COMPLETED) }
            ?: return Result.Failure(DataError.Remote.NOT_FOUND)
        return Result.Success(Unit)
    }

    override suspend fun inviteParticipant(
        hangoutId: String,
        userId: String
    ): Result<HangoutParticipant, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        val invitee = HangoutParticipant(
            user = hangoutUser(userId),
            rsvpStatus = RsvpStatus.PENDING,
            hasPaid = false
        )
        changeHangout(hangoutId) { it.withParticipants(it.participants + invitee) }
            ?: return Result.Failure(DataError.Remote.NOT_FOUND)
        return Result.Success(invitee)
    }

    override suspend fun updateRsvp(
        hangoutId: String,
        rsvpStatus: RsvpStatus
    ): Result<HangoutParticipant, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        rsvpErrorToReturn?.let { return Result.Failure(it) }

        val hangout = hangouts.find { it.id == hangoutId }
            ?: return Result.Failure(DataError.Remote.NOT_FOUND)
        val answered = hangout.participants.find { it.user.userId == currentUserId }
            ?.copy(rsvpStatus = rsvpStatus)
            ?: return Result.Failure(DataError.Remote.NOT_FOUND)

        changeHangout(hangoutId) { current ->
            current.withParticipants(
                current.participants.map { if (it.user.userId == currentUserId) answered else it }
            )
        }
        return Result.Success(answered)
    }

    override suspend fun removeParticipant(
        hangoutId: String,
        userId: String
    ): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        changeHangout(hangoutId) { current ->
            current.withParticipants(current.participants.filterNot { it.user.userId == userId })
        } ?: return Result.Failure(DataError.Remote.NOT_FOUND)
        return Result.Success(Unit)
    }

    override suspend fun leaveHangout(hangoutId: String): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        changeHangout(hangoutId) { current ->
            current.withParticipants(current.participants.filterNot { it.user.userId == currentUserId })
        } ?: return Result.Failure(DataError.Remote.NOT_FOUND)
        return Result.Success(Unit)
    }

    // Replaces the stored hangout with its changed copy; null when no hangout has that id
    private fun changeHangout(hangoutId: String, change: (Hangout) -> Hangout): Hangout? {
        val index = hangouts.indexOfFirst { it.id == hangoutId }
        if (index == -1) return null

        val changed = change(hangouts[index])
        hangouts[index] = changed
        return changed
    }

    private fun Hangout.withParticipants(participants: List<HangoutParticipant>) = copy(
        participants = participants,
        participantCount = participants.count { it.rsvpStatus == RsvpStatus.ATTENDING }
    )

    private fun hangoutUser(userId: String) = HangoutUser(
        userId = userId,
        username = userId,
        displayName = userId,
        profilePictureUrl = null
    )
}
