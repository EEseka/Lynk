package com.eeseka.lynk.shared.data.lobby.dto

import com.eeseka.lynk.shared.data.spot.dto.SpotDto
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import kotlinx.serialization.Serializable

enum class IncomingLobbyMessageType {
    PARTICIPANT_INVITED,
    RSVP_UPDATED,
    INVITE_WITHDRAWN,
    PARTICIPANT_LEFT,
    HANGOUT_UPDATED,
    HANGOUT_COMPLETED,
    HANGOUT_CANCELLED,
    PRESENCE_UPDATE,
    VOTING_SNAPSHOT,
    CANDIDATE_ADDED,
    CANDIDATE_REMOVED,
    VOTE_TALLY,
    CENTER_UPDATE,
    VOTING_TIE,
    ERROR
}

@Serializable
data class LobbyParticipantDto(
    val hangoutId: String,
    val userId: String,
    val displayName: String
)

@Serializable
data class LobbyRsvpDto(
    val hangoutId: String,
    val userId: String,
    val displayName: String,
    val rsvpStatus: RsvpStatus
)

@Serializable
data class LobbyHostActionDto(
    val hangoutId: String,
    val hostDisplayName: String
)

@Serializable
data class PresenceDto(
    val hangoutId: String,
    val presentUserIds: Set<String>
)

@Serializable
data class VotingSnapshotDto(
    val hangoutId: String,
    val candidates: List<SpotDto>,
    val votes: Map<String, String>,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class CandidateAddedDto(
    val hangoutId: String,
    val spot: SpotDto
)

@Serializable
data class CandidateRemovedDto(
    val hangoutId: String,
    val spotId: String
)

@Serializable
data class VoteTallyDto(
    val hangoutId: String,
    val votes: Map<String, String>
)

@Serializable
data class CenterUpdateDto(
    val hangoutId: String,
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class VotingTieDto(
    val hangoutId: String,
    val tiedSpotIds: List<String>
)

@Serializable
data class ErrorDto(
    val code: String,
    val message: String
)
