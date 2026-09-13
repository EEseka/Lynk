package com.eeseka.lynk.shared.data.hangout

import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.hangout.HangoutDetailRepository
import com.eeseka.lynk.shared.domain.hangout.HangoutService
import com.eeseka.lynk.shared.domain.hangout.model.Hangout
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.asEmptyResult
import com.eeseka.lynk.shared.domain.util.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class InMemoryHangoutDetailRepository(
    private val hangoutService: HangoutService,
    sessionStorage: SessionStorage,
    applicationScope: CoroutineScope
) : HangoutDetailRepository {

    // Kept per hangout id, so a late response for one hangout can never land on another.
    private val hangouts = MutableStateFlow<Map<String, Hangout>>(emptyMap())

    init {
        // This outlives sign-out, so the next user must never be shown the last user's hangouts.
        sessionStorage
            .observeAuthInfo()
            .map { authInfo -> authInfo?.user?.id }
            .distinctUntilChanged()
            .onEach { hangouts.value = emptyMap() }
            .launchIn(applicationScope)
    }

    override fun observeHangout(hangoutId: String): Flow<Hangout?> {
        return hangouts
            .map { it[hangoutId] }
            .distinctUntilChanged()
    }

    override suspend fun refreshHangout(hangoutId: String): EmptyResult<DataError.Remote> {
        return hangoutService
            .getHangoutDetails(hangoutId)
            .onSuccess { hangout ->
                hangouts.update { it + (hangoutId to hangout) }
            }
            .asEmptyResult()
    }
}
