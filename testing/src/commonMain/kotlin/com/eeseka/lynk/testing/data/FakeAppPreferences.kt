package com.eeseka.lynk.testing.data

import com.eeseka.lynk.shared.domain.settings.AppPreferences
import com.eeseka.lynk.shared.domain.settings.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAppPreferences : AppPreferences {
    private val themeFlow = MutableStateFlow(AppTheme.SYSTEM)
    override val theme: Flow<AppTheme> = themeFlow

    private val isPureBlackEnabledFlow = MutableStateFlow(false)
    override val isPureBlackEnabled: Flow<Boolean> = isPureBlackEnabledFlow

    private val arePushNotificationsEnabledFlow = MutableStateFlow(true)
    override val arePushNotificationsEnabled: Flow<Boolean> = arePushNotificationsEnabledFlow

    override suspend fun setTheme(theme: AppTheme) {
        themeFlow.value = theme
    }

    override suspend fun setPureBlackEnabled(isEnabled: Boolean) {
        isPureBlackEnabledFlow.value = isEnabled
    }

    override suspend fun setPushNotificationsEnabled(isEnabled: Boolean) {
        arePushNotificationsEnabledFlow.value = isEnabled
    }
}
