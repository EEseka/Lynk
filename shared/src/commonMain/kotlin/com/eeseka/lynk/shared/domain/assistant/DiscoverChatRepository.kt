package com.eeseka.lynk.shared.domain.assistant

interface DiscoverChatRepository {

    fun getChatId(): String?

    fun setChatId(chatId: String?)
}
