package com.eeseka.lynk.shared.data.lobby

import com.eeseka.lynk.shared.data.lobby.dto.CandidateAddedDto
import com.eeseka.lynk.shared.data.lobby.dto.VotingSnapshotDto
import com.eeseka.lynk.shared.data.lobby.dto.CandidateRemovedDto
import com.eeseka.lynk.shared.data.lobby.dto.CenterUpdateDto
import com.eeseka.lynk.shared.data.lobby.dto.ErrorDto
import com.eeseka.lynk.shared.data.lobby.dto.IncomingLobbyMessageType
import com.eeseka.lynk.shared.data.lobby.dto.LobbyHangoutDto
import com.eeseka.lynk.shared.data.lobby.dto.LobbyHostActionDto
import com.eeseka.lynk.shared.data.lobby.dto.LobbyParticipantDto
import com.eeseka.lynk.shared.data.lobby.dto.LobbyPayoutDto
import com.eeseka.lynk.shared.data.lobby.dto.LobbyRsvpDto
import com.eeseka.lynk.shared.data.lobby.dto.PresenceDto
import com.eeseka.lynk.shared.data.lobby.dto.VoteTallyDto
import com.eeseka.lynk.shared.data.lobby.dto.VotingTieDto
import com.eeseka.lynk.shared.data.lobby.dto.WebSocketMessageDto
import com.eeseka.lynk.shared.data.lobby.network.KtorLobbyWebSocketConnector
import com.eeseka.lynk.shared.data.spot.mappers.toDomain
import com.eeseka.lynk.shared.domain.lobby.LobbyConnectionClient
import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.json.Json

class WebSocketLobbyConnectionClient(
    private val webSocketConnector: KtorLobbyWebSocketConnector,
    private val json: Json,
    private val applicationScope: CoroutineScope
) : LobbyConnectionClient {

    override val events = webSocketConnector
        .messages
        .mapNotNull { parseIncomingMessage(it) }
        .shareIn(
            applicationScope,
            SharingStarted.WhileSubscribed(5000)
        )

    override val connectionState = webSocketConnector.connectionState

    private fun parseIncomingMessage(message: WebSocketMessageDto): LobbyEvent? {
        val type = IncomingLobbyMessageType.entries.firstOrNull { it.name == message.type }
            ?: return null

        return when (type) {
            IncomingLobbyMessageType.PARTICIPANT_INVITED -> {
                val dto = json.decodeFromString<LobbyParticipantDto>(message.payload)
                LobbyEvent.ParticipantInvited(dto.hangoutId, dto.userId, dto.displayName)
            }

            IncomingLobbyMessageType.INVITE_WITHDRAWN -> {
                val dto = json.decodeFromString<LobbyParticipantDto>(message.payload)
                LobbyEvent.ParticipantInviteWithdrawn(dto.hangoutId, dto.userId, dto.displayName)
            }

            IncomingLobbyMessageType.NON_PAYER_REMOVED -> {
                val dto = json.decodeFromString<LobbyParticipantDto>(message.payload)
                LobbyEvent.NonPayerRemoved(dto.hangoutId, dto.userId, dto.displayName)
            }

            IncomingLobbyMessageType.PARTICIPANT_LEFT -> {
                val dto = json.decodeFromString<LobbyParticipantDto>(message.payload)
                LobbyEvent.ParticipantLeft(dto.hangoutId, dto.userId, dto.displayName)
            }

            IncomingLobbyMessageType.PAYMENT_RECEIVED -> {
                val dto = json.decodeFromString<LobbyParticipantDto>(message.payload)
                LobbyEvent.PaymentReceived(dto.hangoutId, dto.userId, dto.displayName)
            }

            IncomingLobbyMessageType.PAYMENT_DEADLINE_RESOLVED -> {
                val dto = json.decodeFromString<LobbyHangoutDto>(message.payload)
                LobbyEvent.PaymentDeadlineResolved(dto.hangoutId)
            }

            IncomingLobbyMessageType.PAYOUT_OUTCOME -> {
                val dto = json.decodeFromString<LobbyPayoutDto>(message.payload)
                LobbyEvent.PayoutOutcome(dto.hangoutId, dto.succeeded)
            }

            IncomingLobbyMessageType.RSVP_UPDATED -> {
                val dto = json.decodeFromString<LobbyRsvpDto>(message.payload)
                LobbyEvent.RsvpUpdated(dto.hangoutId, dto.userId, dto.displayName, dto.rsvpStatus)
            }

            IncomingLobbyMessageType.HANGOUT_UPDATED -> {
                val dto = json.decodeFromString<LobbyHostActionDto>(message.payload)
                LobbyEvent.HangoutUpdated(dto.hangoutId, dto.hostDisplayName)
            }

            IncomingLobbyMessageType.HANGOUT_COMPLETED -> {
                val dto = json.decodeFromString<LobbyHostActionDto>(message.payload)
                LobbyEvent.HangoutCompleted(dto.hangoutId, dto.hostDisplayName)
            }

            IncomingLobbyMessageType.HANGOUT_CANCELLED -> {
                val dto = json.decodeFromString<LobbyHostActionDto>(message.payload)
                LobbyEvent.HangoutCancelled(dto.hangoutId, dto.hostDisplayName)
            }

            IncomingLobbyMessageType.PRESENCE_UPDATE -> {
                val dto = json.decodeFromString<PresenceDto>(message.payload)
                LobbyEvent.PresenceUpdate(dto.hangoutId, dto.presentUserIds)
            }

            IncomingLobbyMessageType.VOTING_SNAPSHOT -> {
                val dto = json.decodeFromString<VotingSnapshotDto>(message.payload)
                LobbyEvent.VotingSnapshot(
                    hangoutId = dto.hangoutId,
                    candidates = dto.candidates.map { it.toDomain() },
                    votes = dto.votes,
                    latitude = dto.latitude,
                    longitude = dto.longitude
                )
            }

            IncomingLobbyMessageType.CANDIDATE_ADDED -> {
                val dto = json.decodeFromString<CandidateAddedDto>(message.payload)
                LobbyEvent.CandidateAdded(dto.hangoutId, dto.spot.toDomain())
            }

            IncomingLobbyMessageType.CANDIDATE_REMOVED -> {
                val dto = json.decodeFromString<CandidateRemovedDto>(message.payload)
                LobbyEvent.CandidateRemoved(dto.hangoutId, dto.spotId)
            }

            IncomingLobbyMessageType.VOTE_TALLY -> {
                val dto = json.decodeFromString<VoteTallyDto>(message.payload)
                LobbyEvent.VoteTally(dto.hangoutId, dto.votes)
            }

            IncomingLobbyMessageType.CENTER_UPDATE -> {
                val dto = json.decodeFromString<CenterUpdateDto>(message.payload)
                LobbyEvent.CenterUpdate(dto.hangoutId, dto.latitude, dto.longitude)
            }

            IncomingLobbyMessageType.VOTING_TIE -> {
                val dto = json.decodeFromString<VotingTieDto>(message.payload)
                LobbyEvent.VotingTie(dto.hangoutId, dto.tiedSpotIds)
            }

            IncomingLobbyMessageType.ERROR -> {
                val dto = json.decodeFromString<ErrorDto>(message.payload)
                LobbyEvent.LobbyError(dto.code, dto.message)
            }
        }
    }
}
