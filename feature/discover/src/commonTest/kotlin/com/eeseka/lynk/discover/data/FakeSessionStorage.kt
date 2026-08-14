package com.eeseka.lynk.discover.data

import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSessionStorage : SessionStorage {
    private val authInfoFlow = MutableStateFlow<AuthInfo?>(null)

    override fun observeAuthInfo(): Flow<AuthInfo?> = authInfoFlow

    override suspend fun set(info: AuthInfo?) {
        authInfoFlow.value = info
    }
}