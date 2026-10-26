package com.eeseka.lynk.shared.domain.assistant

import com.eeseka.lynk.shared.domain.assistant.model.AssistantMessage
import com.eeseka.lynk.shared.domain.assistant.model.AssistantStreamEvent
import com.eeseka.lynk.shared.domain.assistant.model.AssistantUsage
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result

interface AssistantService {

    /** Asks Lynk AI one question and hands each part of the answer to [onEvent] as it streams in.*/
    suspend fun sendMessage(
        hangoutId: String?,
        discoverChatId: String?,
        latitude: Double?,
        longitude: Double?,
        content: String,
        onEvent: suspend (AssistantStreamEvent) -> Unit
    ): EmptyResult<DataError.Remote>

    suspend fun getHangoutMessages(
        hangoutId: String,
        before: String? = null
    ): Result<List<AssistantMessage>, DataError.Remote>

    suspend fun getDiscoverMessages(discoverChatId: String): Result<List<AssistantMessage>, DataError.Remote>

    suspend fun getHangoutMessageCount(hangoutId: String): Result<Long, DataError.Remote>

    suspend fun clearHangoutChat(hangoutId: String): EmptyResult<DataError.Remote>

    suspend fun getUsage(): Result<AssistantUsage, DataError.Remote>
}
