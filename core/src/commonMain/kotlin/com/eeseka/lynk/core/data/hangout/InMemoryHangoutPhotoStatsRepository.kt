package com.eeseka.lynk.core.data.hangout

import com.eeseka.lynk.core.domain.auth.SessionStorage
import com.eeseka.lynk.core.domain.hangout.HangoutPhotoService
import com.eeseka.lynk.core.domain.hangout.HangoutPhotoStatsRepository
import com.eeseka.lynk.core.domain.hangout.model.HangoutPhotoStats
import com.eeseka.lynk.core.domain.util.DataError
import com.eeseka.lynk.core.domain.util.EmptyResult
import com.eeseka.lynk.core.domain.util.asEmptyResult
import com.eeseka.lynk.core.domain.util.onFailure
import com.eeseka.lynk.core.domain.util.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class InMemoryHangoutPhotoStatsRepository(
    private val hangoutPhotoService: HangoutPhotoService,
    sessionStorage: SessionStorage,
    applicationScope: CoroutineScope
) : HangoutPhotoStatsRepository {

    // Kept per hangout id, so a late response for one hangout can never land on another.
    private val photoStats = MutableStateFlow<Map<String, HangoutPhotoStats>>(emptyMap())

    init {
        // This outlives sign-out, and my photo count belongs to whoever was signed in.
        sessionStorage
            .observeAuthInfo()
            .map { authInfo -> authInfo?.user?.id }
            .distinctUntilChanged()
            .onEach { photoStats.value = emptyMap() }
            .launchIn(applicationScope)
    }

    override fun observePhotoStats(hangoutId: String): Flow<HangoutPhotoStats?> {
        return photoStats
            .map { it[hangoutId] }
            .distinctUntilChanged()
    }

    override suspend fun refreshPhotoStats(hangoutId: String): EmptyResult<DataError.Remote> {
        return hangoutPhotoService
            .getPhotoStats(hangoutId)
            .onSuccess { stats ->
                photoStats.update { it + (hangoutId to stats) }
            }
            .onFailure { error ->
                if (error == DataError.Remote.NOT_FOUND || error == DataError.Remote.FORBIDDEN) {
                    photoStats.update { it - hangoutId }
                }
            }
            .asEmptyResult()
    }
}
