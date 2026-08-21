package com.eeseka.lynk.shared.domain.settings

import kotlinx.coroutines.flow.Flow

interface AppPreferences {
    val theme: Flow<AppTheme>
    val hasSeenOnboarding: Flow<Boolean>
    val arePushNotificationsEnabled: Flow<Boolean>

    suspend fun setTheme(theme: AppTheme)
    suspend fun setOnboardingCompleted()
    suspend fun setPushNotificationsEnabled(isEnabled: Boolean)
}