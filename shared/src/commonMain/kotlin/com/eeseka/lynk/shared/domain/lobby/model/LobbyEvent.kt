package com.eeseka.lynk.shared.domain.lobby.model

import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.spot.model.Spot

sealed interface LobbyEvent {
    data class ParticipantInvited(
        val hangoutId: String,
        val userId: String,
        val displayName: String
    ) : LobbyEvent

    data class ParticipantInviteWithdrawn(
        val hangoutId: String,
        val userId: String,
        val displayName: String
    ) : LobbyEvent

    data class ParticipantLeft(
        val hangoutId: String,
        val userId: String,
        val displayName: String
    ) : LobbyEvent

    data class RsvpUpdated(
        val hangoutId: String,
        val userId: String,
        val displayName: String,
        val rsvpStatus: RsvpStatus
    ) : LobbyEvent

    data class HangoutUpdated(
        val hangoutId: String,
        val hostDisplayName: String
    ) : LobbyEvent

    data class HangoutCompleted(
        val hangoutId: String,
        val hostDisplayName: String
    ) : LobbyEvent

    data class HangoutCancelled(
        val hangoutId: String,
        val hostDisplayName: String
    ) : LobbyEvent

    data class PresenceUpdate(
        val hangoutId: String,
        val presentUserIds: Set<String>
    ) : LobbyEvent

    data class VotingSnapshot(
        val hangoutId: String,
        val candidates: List<Spot>,
        val votes: Map<String, String>,
        val latitude: Double?,
        val longitude: Double?
    ) : LobbyEvent

    data class CandidateAdded(
        val hangoutId: String,
        val spot: Spot
    ) : LobbyEvent

    data class CandidateRemoved(
        val hangoutId: String,
        val spotId: String
    ) : LobbyEvent

    data class VoteTally(
        val hangoutId: String,
        val votes: Map<String, String>
    ) : LobbyEvent

    data class CenterUpdate(
        val hangoutId: String,
        val latitude: Double,
        val longitude: Double
    ) : LobbyEvent

    data class VotingTie(
        val hangoutId: String,
        val tiedSpotIds: List<String>
    ) : LobbyEvent

    data class LobbyError(
        val code: String,
        val message: String
    ) : LobbyEvent
}
