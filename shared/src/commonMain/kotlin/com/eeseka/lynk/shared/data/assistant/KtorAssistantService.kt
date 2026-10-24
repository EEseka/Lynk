package com.eeseka.lynk.shared.data.assistant

import com.eeseka.lynk.shared.data.assistant.dto.AssistantMessageCountDto
import com.eeseka.lynk.shared.data.assistant.dto.AssistantMessageDto
import com.eeseka.lynk.shared.data.assistant.dto.AssistantUsageDto
import com.eeseka.lynk.shared.data.assistant.dto.requests.SendAssistantMessageRequest
import com.eeseka.lynk.shared.data.assistant.dto.sse.AssistantSseEventDto
import com.eeseka.lynk.shared.data.assistant.dto.sse.AssistantSseEventType
import com.eeseka.lynk.shared.data.assistant.mappers.toDomain
import com.eeseka.lynk.shared.data.networking.constructRoute
import com.eeseka.lynk.shared.data.networking.delete
import com.eeseka.lynk.shared.data.networking.get
import com.eeseka.lynk.shared.data.networking.platformSafeCall
import com.eeseka.lynk.shared.data.networking.responseToResult
import com.eeseka.lynk.shared.domain.assistant.AssistantService
import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessage
import com.eeseka.lynk.shared.domain.assistant.model.AssistantStreamEvent
import com.eeseka.lynk.shared.domain.assistant.model.AssistantUsage
import com.eeseka.lynk.shared.domain.logging.LynkLogger
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result
import com.eeseka.lynk.shared.domain.util.map
import io.ktor.client.HttpClient
import io.ktor.client.plugins.sse.SSEClientException
import io.ktor.client.plugins.sse.sse
import io.ktor.client.plugins.timeout
import io.ktor.client.request.accept
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.sse.ServerSentEvent
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json

class KtorAssistantService(
    private val httpClient: HttpClient,
    private val json: Json,
    private val logger: LynkLogger
) : AssistantService {

    companion object {
        private const val STREAM_MAX_DURATION_MILLIS = 100_000L // Past the server's own 90 s stream limit
        private const val STREAM_MAX_SILENCE_MILLIS = 35_000L // Two missed server pings, which come every 15 s
    }

    override suspend fun sendMessage(
        hangoutId: String?,
        discoverChatId: String?,
        latitude: Double?,
        longitude: Double?,
        content: String,
        onEvent: suspend (AssistantStreamEvent) -> Unit
    ): EmptyResult<DataError.Remote> {
        return try {
            httpClient.sse(
                request = {
                    method = HttpMethod.Post
                    url(constructRoute("/assistant/messages"))
                    accept(ContentType.Application.Json) // Refusals (limit reached, answer in progress) come back as JSON errors, not a stream
                    setBody(
                        SendAssistantMessageRequest(
                            hangoutId = hangoutId,
                            discoverChatId = discoverChatId,
                            latitude = latitude,
                            longitude = longitude,
                            timeZone = TimeZone.currentSystemDefault().id,
                            content = content
                        )
                    )
                    timeout {
                        requestTimeoutMillis = STREAM_MAX_DURATION_MILLIS
                        socketTimeoutMillis = STREAM_MAX_SILENCE_MILLIS
                    }
                }
            ) {
                incoming.collect { event ->
                    event.toStreamEvent()?.let { onEvent(it) }
                }
            }
            Result.Success(Unit)
        } catch (e: SSEClientException) {
            val response = e.response
            if (response != null && !response.status.isSuccess()) {
                responseToResult(response)
            } else {
                logger.error("Assistant stream failed", e.cause ?: e)
                // Borrows the REST error mapping for a stream that failed or dropped
                platformSafeCall(execute = { throw e.cause ?: e }) { Result.Success(Unit) }
            }
        }
    }

    override suspend fun getHangoutMessages(
        hangoutId: String,
        before: String?
    ): Result<List<AssistantMessage>, DataError.Remote> {
        return httpClient.get<List<AssistantMessageDto>>(
            route = "/assistant/hangouts/$hangoutId/messages",
            queryParams = buildMap {
                put("pageSize", AssistantConstants.PAGE_SIZE)
                before?.let { put("before", it) }
            }
        ).map { messages ->
            messages.map { it.toDomain() }
        }
    }

    override suspend fun getHangoutMessageCount(hangoutId: String): Result<Long, DataError.Remote> {
        return httpClient.get<AssistantMessageCountDto>(
            route = "/assistant/hangouts/$hangoutId/messages/count"
        ).map { it.count }
    }

    override suspend fun clearHangoutChat(hangoutId: String): EmptyResult<DataError.Remote> {
        return httpClient.delete<Unit>(
            route = "/assistant/hangouts/$hangoutId/messages"
        )
    }

    override suspend fun getUsage(): Result<AssistantUsage, DataError.Remote> {
        return httpClient.get<AssistantUsageDto>(
            route = "/assistant/usage"
        ).map { it.toDomain() }
    }

    private fun ServerSentEvent.toStreamEvent(): AssistantStreamEvent? {
        val type = AssistantSseEventType.entries.firstOrNull { it.name == event } ?: return null
        val payload = data.orEmpty()

        return when (type) {
            AssistantSseEventType.STARTED -> {
                val dto = json.decodeFromString<AssistantSseEventDto.StartedDto>(payload)
                AssistantStreamEvent.Started(dto.messageId, dto.discoverChatId)
            }

            AssistantSseEventType.STATUS -> {
                val dto = json.decodeFromString<AssistantSseEventDto.StatusDto>(payload)
                AssistantStreamEvent.Status(dto.kind)
            }

            AssistantSseEventType.TEXT -> {
                val dto = json.decodeFromString<AssistantSseEventDto.TextDto>(payload)
                AssistantStreamEvent.Text(dto.text)
            }

            AssistantSseEventType.SPOTS -> {
                val dto = json.decodeFromString<AssistantSseEventDto.SpotsDto>(payload)
                AssistantStreamEvent.Spots(dto.recommendations.map { it.toDomain() })
            }

            AssistantSseEventType.DONE -> AssistantStreamEvent.Done

            AssistantSseEventType.ERROR -> {
                val dto = json.decodeFromString<AssistantSseEventDto.ErrorDto>(payload)
                AssistantStreamEvent.Error(dto.reason)
            }
        }
    }
}
