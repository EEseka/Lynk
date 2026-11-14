package com.eeseka.lynk.core.domain.assistant

import com.eeseka.lynk.core.domain.util.DataError
import com.eeseka.lynk.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow

interface AssistantMessageCountRepository {

    fun observeMessageCount(hangoutId: String): Flow<Long?>

    suspend fun refreshMessageCount(hangoutId: String): EmptyResult<DataError.Remote>
}
