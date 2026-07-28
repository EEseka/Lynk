package com.eeseka.lynk.shared.data.lobby

import com.eeseka.lynk.shared.data.lobby.dto.OutgoingLobbyDto
import com.eeseka.lynk.shared.data.lobby.dto.WebSocketMessageDto
import com.eeseka.lynk.shared.data.lobby.network.KtorLobbyWebSocketConnector
import com.eeseka.lynk.shared.domain.lobby.LobbyService
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import kotlinx.serialization.json.Json

class WebSocketLobbyService(
    private val webSocketConnector: KtorLobbyWebSocketConnector,
    private val json: Json
) : LobbyService {

    override suspend fun enterLobby(hangoutId: String): EmptyResult<DataError.Connection> {
        return webSocketConnector.sendMessage(
            OutgoingLobbyDto.EnterLobby(hangoutId).toJsonPayload()
        )
    }

    override suspend fun leaveLobby(hangoutId: String): EmptyResult<DataError.Connection> {
        return webSocketConnector.sendMessage(
            OutgoingLobbyDto.LeaveLobby(hangoutId).toJsonPayload()
        )
    }

    override suspend fun shareLocation(
        hangoutId: String,
        latitude: Double,
        longitude: Double
    ): EmptyResult<DataError.Connection> {
        return webSocketConnector.sendMessage(
            OutgoingLobbyDto.LocationShare(hangoutId, latitude, longitude).toJsonPayload()
        )
    }

    override suspend fun proposeSpot(
        hangoutId: String,
        spotId: String
    ): EmptyResult<DataError.Connection> {
        return webSocketConnector.sendMessage(
            OutgoingLobbyDto.ProposeSpot(hangoutId, spotId).toJsonPayload()
        )
    }

    override suspend fun removeSpot(
        hangoutId: String,
        spotId: String
    ): EmptyResult<DataError.Connection> {
        return webSocketConnector.sendMessage(
            OutgoingLobbyDto.RemoveSpot(hangoutId, spotId).toJsonPayload()
        )
    }

    override suspend fun castVote(
        hangoutId: String,
        spotId: String
    ): EmptyResult<DataError.Connection> {
        return webSocketConnector.sendMessage(
            OutgoingLobbyDto.CastVote(hangoutId, spotId).toJsonPayload()
        )
    }

    override suspend fun closeVoting(
        hangoutId: String,
        chosenSpotId: String?
    ): EmptyResult<DataError.Connection> {
        return webSocketConnector.sendMessage(
            OutgoingLobbyDto.CloseVoting(hangoutId, chosenSpotId).toJsonPayload()
        )
    }

    private inline fun <reified T : OutgoingLobbyDto> T.toJsonPayload(): String {
        val webSocketMessage = WebSocketMessageDto(
            type = type.name,
            payload = json.encodeToString(this)
        )
        return json.encodeToString(webSocketMessage)
    }
}
