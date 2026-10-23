package com.eeseka.lynk.shared.data.lobby.dto.ws

import com.eeseka.lynk.shared.data.spot.dto.SpotDto
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import kotlinx.serialization.Serializable

enum class IncomingLobbyMessageType {
    PARTICIPANT_INVITED,
    RSVP_UPDATED,
    INVITE_WITHDRAWN,
    NON_PAYER_REMOVED,
    PARTICIPANT_LEFT,
    PAYMENT_RECEIVED,
    PAYMENT_DEADLINE_RESOLVED,
    PAYOUT_OUTCOME,
    HANGOUT_UPDATED,
    HANGOUT_STARTED,
    HANGOUT_COMPLETED,
    HANGOUT_CANCELLED,
    PHOTOS_ADDED,
    PHOTO_DELETED,
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
sealed interface IncomingLobbyDto {

    @Serializable
    data class LobbyParticipantDto(
        val hangoutId: String,
        val userId: String,
        val displayName: String
    ) : IncomingLobbyDto

    @Serializable
    data class LobbyHangoutDto(
        val hangoutId: String
    ) : IncomingLobbyDto

    @Serializable
    data class LobbyPayoutDto(
        val hangoutId: String,
        val succeeded: Boolean
    ) : IncomingLobbyDto

    @Serializable
    data class LobbyRsvpDto(
        val hangoutId: String,
        val userId: String,
        val displayName: String,
        val rsvpStatus: RsvpStatus
    ) : IncomingLobbyDto

    @Serializable
    data class LobbyHostActionDto(
        val hangoutId: String,
        val hostDisplayName: String
    ) : IncomingLobbyDto

    @Serializable
    data class LobbyPhotosAddedDto(
        val hangoutId: String,
        val uploaderIds: Set<String>
    ) : IncomingLobbyDto

    @Serializable
    data class LobbyPhotoDto(
        val hangoutId: String,
        val photoId: String
    ) : IncomingLobbyDto

    @Serializable
    data class PresenceDto(
        val hangoutId: String,
        val presentUserIds: Set<String>
    ) : IncomingLobbyDto

    @Serializable
    data class VotingSnapshotDto(
        val hangoutId: String,
        val candidates: List<SpotDto>,
        val votes: Map<String, String>,
        val latitude: Double? = null,
        val longitude: Double? = null
    ) : IncomingLobbyDto

    @Serializable
    data class CandidateAddedDto(
        val hangoutId: String,
        val spot: SpotDto
    ) : IncomingLobbyDto

    @Serializable
    data class CandidateRemovedDto(
        val hangoutId: String,
        val spotId: String
    ) : IncomingLobbyDto

    @Serializable
    data class VoteTallyDto(
        val hangoutId: String,
        val votes: Map<String, String>
    ) : IncomingLobbyDto

    @Serializable
    data class CenterUpdateDto(
        val hangoutId: String,
        val latitude: Double,
        val longitude: Double
    ) : IncomingLobbyDto

    @Serializable
    data class VotingTieDto(
        val hangoutId: String,
        val tiedSpotIds: List<String>
    ) : IncomingLobbyDto

    @Serializable
    data class ErrorDto(
        val code: String,
        val message: String
    ) : IncomingLobbyDto
}
