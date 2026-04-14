package com.eeseka.lynk.auth.data

import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeSessionStorage : SessionStorage {
    private var storedAuthInfo: AuthInfo? = null

    fun get(): AuthInfo? {
        return storedAuthInfo
    }

    override suspend fun set(info: AuthInfo?) {
        storedAuthInfo = info
    }

    // In a real app this uses DataStore, but in our fake it just emits a single value
    override fun observeAuthInfo(): Flow<AuthInfo?> {
        return flowOf(storedAuthInfo)
    }
}