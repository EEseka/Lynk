package com.eeseka.lynk.testing.data

import com.eeseka.lynk.shared.data.assistant.AssistantConstants
import com.eeseka.lynk.shared.domain.assistant.AssistantService
import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessage
import com.eeseka.lynk.shared.domain.assistant.model.AssistantStreamEvent
import com.eeseka.lynk.shared.domain.assistant.model.AssistantUsage
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result
import kotlin.time.Instant

class FakeAssistantService : AssistantService {
    data class SentMessage(
        val hangoutId: String?,
        val discoverChatId: String?,
        val latitude: Double?,
        val longitude: Double?,
        val content: String
    )

    var shouldReturnError = false
    var errorToReturn = DataError.Remote.SERVER_ERROR

    // A question the server refuses before any answer streams
    var refusal: DataError.Remote? = null
    // What the next answer streams, in order
    var streamEvents = mutableListOf<AssistantStreamEvent>()
    // How the stream ends; a failure stands in for a dropped connection
    var streamEndResult: EmptyResult<DataError.Remote> = Result.Success(Unit)
    val sentMessages = mutableListOf<SentMessage>()

    var hangoutMessages = mutableListOf<AssistantMessage>()
    val historyRequestBefores = mutableListOf<String?>()
    val messageCountRequestIds = mutableListOf<String>()
    val clearedHangoutIds = mutableListOf<String>()

    val discoverChats = mutableMapOf<String, List<AssistantMessage>>()
    val discoverRequestChatIds = mutableListOf<String>()

    var usage = AssistantUsage(used = 0, limit = 20, resetsInSeconds = null)
    var usageRequestCount = 0

    override suspend fun sendMessage(
        hangoutId: String?,
        discoverChatId: String?,
        latitude: Double?,
        longitude: Double?,
        content: String,
        onEvent: suspend (AssistantStreamEvent) -> Unit
    ): EmptyResult<DataError.Remote> {
        sentMessages.add(SentMessage(hangoutId, discoverChatId, latitude, longitude, content))
        refusal?.let { return Result.Failure(it) }

        streamEvents.forEach { onEvent(it) }
        return streamEndResult
    }

    override suspend fun getHangoutMessages(
        hangoutId: String,
        before: String?
    ): Result<List<AssistantMessage>, DataError.Remote> {
        historyRequestBefores.add(before)
        if (shouldReturnError) return Result.Failure(errorToReturn)

        val beforeInstant = before?.let { Instant.parse(it) }
        return Result.Success(
            hangoutMessages
                .filter { beforeInstant == null || it.createdAt < beforeInstant }
                .take(AssistantConstants.PAGE_SIZE)
        )
    }

    override suspend fun getDiscoverMessages(discoverChatId: String): Result<List<AssistantMessage>, DataError.Remote> {
        discoverRequestChatIds.add(discoverChatId)
        if (shouldReturnError) return Result.Failure(errorToReturn)
        val messages = discoverChats[discoverChatId] ?: return Result.Failure(DataError.Remote.NOT_FOUND)
        return Result.Success(messages)
    }

    override suspend fun getHangoutMessageCount(hangoutId: String): Result<Long, DataError.Remote> {
        messageCountRequestIds.add(hangoutId)
        if (shouldReturnError) return Result.Failure(errorToReturn)
        return Result.Success(hangoutMessages.size.toLong())
    }

    override suspend fun clearHangoutChat(hangoutId: String): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        clearedHangoutIds.add(hangoutId)
        hangoutMessages.clear()
        return Result.Success(Unit)
    }

    override suspend fun getUsage(): Result<AssistantUsage, DataError.Remote> {
        usageRequestCount++
        if (shouldReturnError) return Result.Failure(errorToReturn)
        return Result.Success(usage)
    }
}
