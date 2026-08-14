package com.eeseka.lynk.shared.data.lobby.dto

import kotlinx.serialization.Serializable

enum class OutgoingLobbyMessageType {
    ENTER_LOBBY,
    LEAVE_LOBBY,
    LOCATION_SHARE,
    PROPOSE_SPOT,
    REMOVE_SPOT,
    CAST_VOTE,
    CLOSE_VOTING
}

@Serializable
sealed class OutgoingLobbyDto(
    val type: OutgoingLobbyMessageType
) {
    @Serializable
    data class EnterLobby(
        val hangoutId: String
    ) : OutgoingLobbyDto(OutgoingLobbyMessageType.ENTER_LOBBY)

    @Serializable
    data class LeaveLobby(
        val hangoutId: String
    ) : OutgoingLobbyDto(OutgoingLobbyMessageType.LEAVE_LOBBY)

    @Serializable
    data class LocationShare(
        val hangoutId: String,
        val latitude: Double,
        val longitude: Double
    ) : OutgoingLobbyDto(OutgoingLobbyMessageType.LOCATION_SHARE)

    @Serializable
    data class ProposeSpot(
        val hangoutId: String,
        val spotId: String
    ) : OutgoingLobbyDto(OutgoingLobbyMessageType.PROPOSE_SPOT)

    @Serializable
    data class RemoveSpot(
        val hangoutId: String,
        val spotId: String
    ) : OutgoingLobbyDto(OutgoingLobbyMessageType.REMOVE_SPOT)

    @Serializable
    data class CastVote(
        val hangoutId: String,
        val spotId: String
    ) : OutgoingLobbyDto(OutgoingLobbyMessageType.CAST_VOTE)

    @Serializable
    data class CloseVoting(
        val hangoutId: String,
        val chosenSpotId: String? = null
    ) : OutgoingLobbyDto(OutgoingLobbyMessageType.CLOSE_VOTING)
}
