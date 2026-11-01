package com.eeseka.lynk.hangouts.presentation.mappers

import com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location.model.LiveLocationUi
import com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location.model.LiveParticipantUi
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.lobby.model.LiveLocation
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutParticipantUi
import kotlin.time.Instant

fun LiveLocation.toLiveLocationUi() = LiveLocationUi(
    latitude = latitude,
    longitude = longitude,
    sharedAt = sharedAt
)

fun List<HangoutParticipantUi>.toLiveParticipants(
    liveLocations: Map<String, LiveLocationUi>,
    currentUserId: String?,
    now: Instant
): List<LiveParticipantUi> {
    return filter { it.user.userId != currentUserId && it.rsvpStatus == RsvpStatus.ATTENDING }
        .sortedBy { it.user.userId }
        .mapNotNull { participant ->
            liveLocations[participant.user.userId]?.let { liveLocation ->
                LiveParticipantUi(
                    participant = participant,
                    latitude = liveLocation.latitude,
                    longitude = liveLocation.longitude,
                    minutesSinceShared = (now - liveLocation.sharedAt).inWholeMinutes.toInt().coerceAtLeast(0)
                )
            }
        }
}
