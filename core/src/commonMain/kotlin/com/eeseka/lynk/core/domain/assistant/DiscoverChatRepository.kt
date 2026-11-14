package com.eeseka.lynk.core.domain.assistant

interface DiscoverChatRepository {

    fun getChatId(): String?

    fun setChatId(chatId: String?)
}
