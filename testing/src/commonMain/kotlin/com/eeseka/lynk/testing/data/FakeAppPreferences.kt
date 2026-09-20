package com.eeseka.lynk.testing.data

import com.eeseka.lynk.shared.domain.settings.AppPreferences
import com.eeseka.lynk.shared.domain.settings.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAppPreferences : AppPreferences {
    private val themeFlow = MutableStateFlow(AppTheme.SYSTEM)
    override val theme: Flow<AppTheme> = themeFlow

    private val arePushNotificationsEnabledFlow = MutableStateFlow(true)
    override val arePushNotificationsEnabled: Flow<Boolean> = arePushNotificationsEnabledFlow

    override suspend fun setTheme(theme: AppTheme) {
        themeFlow.value = theme
    }

    override suspend fun setPushNotificationsEnabled(isEnabled: Boolean) {
        arePushNotificationsEnabledFlow.value = isEnabled
    }
}
