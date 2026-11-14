package com.eeseka.lynk.core.domain.auth

import com.eeseka.lynk.core.domain.auth.model.AuthInfo
import kotlinx.coroutines.flow.Flow

interface SessionStorage {
    fun observeAuthInfo(): Flow<AuthInfo?>
    suspend fun set(info: AuthInfo?)
}