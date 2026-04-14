package com.eeseka.lynk.shared.domain.auth

import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import kotlinx.coroutines.flow.Flow

interface SessionStorage {
    fun observeAuthInfo(): Flow<AuthInfo?>
    suspend fun set(info: AuthInfo?)
}