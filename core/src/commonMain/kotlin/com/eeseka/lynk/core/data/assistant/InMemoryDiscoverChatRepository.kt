package com.eeseka.lynk.core.data.assistant

import com.eeseka.lynk.core.domain.assistant.DiscoverChatRepository
import com.eeseka.lynk.core.domain.auth.SessionStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class InMemoryDiscoverChatRepository(
    sessionStorage: SessionStorage,
    applicationScope: CoroutineScope
) : DiscoverChatRepository {

    private val chatId = MutableStateFlow<String?>(null)

    init {
        sessionStorage
            .observeAuthInfo()
            .map { authInfo -> authInfo?.user?.id }
            .distinctUntilChanged()
            .onEach { chatId.value = null }
            .launchIn(applicationScope)
    }

    override fun getChatId(): String? = chatId.value

    override fun setChatId(chatId: String?) {
        this.chatId.value = chatId
    }
}
