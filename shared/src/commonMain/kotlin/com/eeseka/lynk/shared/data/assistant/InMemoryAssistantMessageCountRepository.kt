package com.eeseka.lynk.shared.data.assistant

import com.eeseka.lynk.shared.domain.assistant.AssistantMessageCountRepository
import com.eeseka.lynk.shared.domain.assistant.AssistantService
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.asEmptyResult
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.domain.util.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class InMemoryAssistantMessageCountRepository(
    private val assistantService: AssistantService,
    sessionStorage: SessionStorage,
    applicationScope: CoroutineScope
) : AssistantMessageCountRepository {

    // Kept per hangout id, so a late response for one hangout can never land on another.
    private val messageCounts = MutableStateFlow<Map<String, Long>>(emptyMap())

    init {
        // This outlives sign-out, and a chat belongs to whoever was signed in.
        sessionStorage
            .observeAuthInfo()
            .map { authInfo -> authInfo?.user?.id }
            .distinctUntilChanged()
            .onEach { messageCounts.value = emptyMap() }
            .launchIn(applicationScope)
    }

    override fun observeMessageCount(hangoutId: String): Flow<Long?> {
        return messageCounts
            .map { it[hangoutId] }
            .distinctUntilChanged()
    }

    override suspend fun refreshMessageCount(hangoutId: String): EmptyResult<DataError.Remote> {
        return assistantService
            .getHangoutMessageCount(hangoutId)
            .onSuccess { count ->
                messageCounts.update { it + (hangoutId to count) }
            }
            .onFailure { error ->
                if (error == DataError.Remote.NOT_FOUND || error == DataError.Remote.FORBIDDEN) {
                    messageCounts.update { it - hangoutId }
                }
            }
            .asEmptyResult()
    }
}
