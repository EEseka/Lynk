package com.eeseka.lynk.shared.data.lobby

import com.eeseka.lynk.shared.data.lobby.dto.ws.IncomingLobbyDto
import com.eeseka.lynk.shared.data.lobby.dto.ws.IncomingLobbyMessageType
import com.eeseka.lynk.shared.data.lobby.dto.ws.WebSocketMessageDto
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

        return try {
            parsePayload(type, message.payload)
        } catch (_: Exception) {
            null
        }
    }

    private fun parsePayload(type: IncomingLobbyMessageType, payload: String): LobbyEvent {
        return when (type) {
            IncomingLobbyMessageType.PARTICIPANT_INVITED -> {
                val dto = json.decodeFromString<IncomingLobbyDto.LobbyParticipantDto>(payload)
                LobbyEvent.ParticipantInvited(dto.hangoutId, dto.userId, dto.displayName)
            }

            IncomingLobbyMessageType.INVITE_WITHDRAWN -> {
                val dto = json.decodeFromString<IncomingLobbyDto.LobbyParticipantDto>(payload)
                LobbyEvent.ParticipantInviteWithdrawn(dto.hangoutId, dto.userId, dto.displayName)
            }

            IncomingLobbyMessageType.NON_PAYER_REMOVED -> {
                val dto = json.decodeFromString<IncomingLobbyDto.LobbyParticipantDto>(payload)
                LobbyEvent.NonPayerRemoved(dto.hangoutId, dto.userId, dto.displayName)
            }

            IncomingLobbyMessageType.PARTICIPANT_LEFT -> {
                val dto = json.decodeFromString<IncomingLobbyDto.LobbyParticipantDto>(payload)
                LobbyEvent.ParticipantLeft(dto.hangoutId, dto.userId, dto.displayName)
            }

            IncomingLobbyMessageType.PAYMENT_RECEIVED -> {
                val dto = json.decodeFromString<IncomingLobbyDto.LobbyParticipantDto>(payload)
                LobbyEvent.PaymentReceived(dto.hangoutId, dto.userId, dto.displayName)
            }

            IncomingLobbyMessageType.PAYMENT_DEADLINE_RESOLVED -> {
                val dto = json.decodeFromString<IncomingLobbyDto.LobbyHangoutDto>(payload)
                LobbyEvent.PaymentDeadlineResolved(dto.hangoutId)
            }

            IncomingLobbyMessageType.PAYOUT_OUTCOME -> {
                val dto = json.decodeFromString<IncomingLobbyDto.LobbyPayoutDto>(payload)
                LobbyEvent.PayoutOutcome(dto.hangoutId, dto.succeeded)
            }

            IncomingLobbyMessageType.RSVP_UPDATED -> {
                val dto = json.decodeFromString<IncomingLobbyDto.LobbyRsvpDto>(payload)
                LobbyEvent.RsvpUpdated(dto.hangoutId, dto.userId, dto.displayName, dto.rsvpStatus)
            }

            IncomingLobbyMessageType.HANGOUT_UPDATED -> {
                val dto = json.decodeFromString<IncomingLobbyDto.LobbyHostActionDto>(payload)
                LobbyEvent.HangoutUpdated(dto.hangoutId, dto.hostDisplayName)
            }

            IncomingLobbyMessageType.HANGOUT_STARTED -> {
                val dto = json.decodeFromString<IncomingLobbyDto.LobbyHangoutDto>(payload)
                LobbyEvent.HangoutStarted(dto.hangoutId)
            }

            IncomingLobbyMessageType.HANGOUT_COMPLETED -> {
                val dto = json.decodeFromString<IncomingLobbyDto.LobbyHostActionDto>(payload)
                LobbyEvent.HangoutCompleted(dto.hangoutId, dto.hostDisplayName)
            }

            IncomingLobbyMessageType.HANGOUT_CANCELLED -> {
                val dto = json.decodeFromString<IncomingLobbyDto.LobbyHostActionDto>(payload)
                LobbyEvent.HangoutCancelled(dto.hangoutId, dto.hostDisplayName)
            }

            IncomingLobbyMessageType.PHOTOS_ADDED -> {
                val dto = json.decodeFromString<IncomingLobbyDto.LobbyPhotosAddedDto>(payload)
                LobbyEvent.PhotosAdded(dto.hangoutId, dto.uploaderIds)
            }

            IncomingLobbyMessageType.PHOTO_DELETED -> {
                val dto = json.decodeFromString<IncomingLobbyDto.LobbyPhotoDto>(payload)
                LobbyEvent.PhotoDeleted(dto.hangoutId, dto.photoId)
            }

            IncomingLobbyMessageType.PRESENCE_UPDATE -> {
                val dto = json.decodeFromString<IncomingLobbyDto.PresenceDto>(payload)
                LobbyEvent.PresenceUpdate(dto.hangoutId, dto.presentUserIds)
            }

            IncomingLobbyMessageType.VOTING_SNAPSHOT -> {
                val dto = json.decodeFromString<IncomingLobbyDto.VotingSnapshotDto>(payload)
                LobbyEvent.VotingSnapshot(
                    hangoutId = dto.hangoutId,
                    candidates = dto.candidates.map { it.toDomain() },
                    votes = dto.votes,
                    latitude = dto.latitude,
                    longitude = dto.longitude
                )
            }

            IncomingLobbyMessageType.CANDIDATE_ADDED -> {
                val dto = json.decodeFromString<IncomingLobbyDto.CandidateAddedDto>(payload)
                LobbyEvent.CandidateAdded(dto.hangoutId, dto.spot.toDomain())
            }

            IncomingLobbyMessageType.CANDIDATE_REMOVED -> {
                val dto = json.decodeFromString<IncomingLobbyDto.CandidateRemovedDto>(payload)
                LobbyEvent.CandidateRemoved(dto.hangoutId, dto.spotId)
            }

            IncomingLobbyMessageType.VOTE_TALLY -> {
                val dto = json.decodeFromString<IncomingLobbyDto.VoteTallyDto>(payload)
                LobbyEvent.VoteTally(dto.hangoutId, dto.votes)
            }

            IncomingLobbyMessageType.CENTER_UPDATE -> {
                val dto = json.decodeFromString<IncomingLobbyDto.CenterUpdateDto>(payload)
                LobbyEvent.CenterUpdate(dto.hangoutId, dto.latitude, dto.longitude)
            }

            IncomingLobbyMessageType.VOTING_TIE -> {
                val dto = json.decodeFromString<IncomingLobbyDto.VotingTieDto>(payload)
                LobbyEvent.VotingTie(dto.hangoutId, dto.tiedSpotIds)
            }

            IncomingLobbyMessageType.ERROR -> {
                val dto = json.decodeFromString<IncomingLobbyDto.ErrorDto>(payload)
                LobbyEvent.LobbyError(dto.code, dto.message)
            }
        }
    }
}
