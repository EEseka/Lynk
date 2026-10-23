package com.eeseka.lynk.shared.domain.assistant

import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow

interface AssistantMessageCountRepository {

    fun observeMessageCount(hangoutId: String): Flow<Long?>

    suspend fun refreshMessageCount(hangoutId: String): EmptyResult<DataError.Remote>
}
