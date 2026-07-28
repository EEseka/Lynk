package com.eeseka.lynk.shared.presentation.hangout.mappers

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.CupSoda
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.Gamepad2
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.PartyPopper
import com.composables.icons.lucide.Sofa
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Utensils
import com.eeseka.lynk.shared.domain.hangout.model.Hangout
import com.eeseka.lynk.shared.domain.hangout.model.HangoutParticipant
import com.eeseka.lynk.shared.domain.hangout.model.HangoutPreview
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutSummary
import com.eeseka.lynk.shared.domain.hangout.model.HangoutUser
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutParticipantUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutPreviewUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutSummaryUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUi
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUserUi
import com.eeseka.lynk.shared.presentation.spot.mappers.toSpotUi
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.active
import lynk.shared.generated.resources.cancelled
import lynk.shared.generated.resources.chill
import lynk.shared.generated.resources.completed
import lynk.shared.generated.resources.drinks
import lynk.shared.generated.resources.food
import lynk.shared.generated.resources.gaming
import lynk.shared.generated.resources.ongoing
import lynk.shared.generated.resources.other
import lynk.shared.generated.resources.party
import lynk.shared.generated.resources.scheduled
import lynk.shared.generated.resources.voting
import org.jetbrains.compose.resources.stringResource

@Composable
fun HangoutVibe.getTitle(): String {
    return stringResource(
        when (this) {
            HangoutVibe.CHILL -> Res.string.chill
            HangoutVibe.FOOD -> Res.string.food
            HangoutVibe.DRINKS -> Res.string.drinks
            HangoutVibe.GAMING -> Res.string.gaming
            HangoutVibe.ACTIVE -> Res.string.active
            HangoutVibe.PARTY -> Res.string.party
            HangoutVibe.OTHER -> Res.string.other
        }
    )
}

fun HangoutVibe.getIcon(): ImageVector {
    return when (this) {
        HangoutVibe.CHILL -> Lucide.Sofa
        HangoutVibe.FOOD -> Lucide.Utensils
        HangoutVibe.DRINKS -> Lucide.CupSoda
        HangoutVibe.GAMING -> Lucide.Gamepad2
        HangoutVibe.ACTIVE -> Lucide.Flame
        HangoutVibe.PARTY -> Lucide.PartyPopper
        HangoutVibe.OTHER -> Lucide.Sparkles
    }
}

@Composable
fun HangoutStatus.getTitle(): String {
    return stringResource(
        when (this) {
            HangoutStatus.VOTING -> Res.string.voting
            HangoutStatus.SCHEDULED -> Res.string.scheduled
            HangoutStatus.ONGOING -> Res.string.ongoing
            HangoutStatus.COMPLETED -> Res.string.completed
            HangoutStatus.CANCELLED -> Res.string.cancelled
        }
    )
}

fun Hangout.toHangoutUi() = HangoutUi(
    id = id,
    hostId = hostId,
    name = name,
    description = description,
    vibe = vibe,
    status = status,
    scheduledAt = scheduledAt,
    maxAttendees = maxAttendees,
    participantCount = participantCount,
    chosenSpot = chosenSpot?.toSpotUi(),
    totalCost = totalCost,
    costPerPerson = costPerPerson,
    participants = participants.map { it.toHangoutParticipantUi() },
    createdAt = createdAt
)

fun HangoutSummary.toHangoutSummaryUi(): HangoutSummaryUi {
    return HangoutSummaryUi(
        id = id,
        hostId = hostId,
        name = name,
        vibe = vibe,
        status = status,
        scheduledAt = scheduledAt,
        maxAttendees = maxAttendees,
        participantCount = participantCount,
        createdAt = createdAt
    )
}

fun HangoutPreview.toHangoutPreviewUi() = HangoutPreviewUi(
    id = id,
    hostId = hostId,
    name = name,
    description = description,
    vibe = vibe,
    status = status,
    scheduledAt = scheduledAt,
    maxAttendees = maxAttendees,
    participantCount = participantCount,
    chosenSpot = chosenSpot?.toSpotUi(),
    attendees = attendees.map { it.toHangoutUserUi() },
    createdAt = createdAt
)

fun HangoutUser.toHangoutUserUi() = HangoutUserUi(
    userId = userId,
    username = username,
    displayName = displayName,
    initials = displayName.toInitials(),
    profilePictureUrl = profilePictureUrl
)

fun HangoutParticipant.toHangoutParticipantUi() = HangoutParticipantUi(
    userId = userId,
    username = username,
    displayName = displayName,
    initials = displayName.toInitials(),
    profilePictureUrl = profilePictureUrl,
    rsvpStatus = rsvpStatus,
    hasPaid = hasPaid
)

private fun String.toInitials(): String {
    val words = trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        words.isEmpty() -> ""
        words.size == 1 -> words.first().take(2).uppercase()
        else -> "${words.first().first()}${words.last().first()}".uppercase()
    }
}