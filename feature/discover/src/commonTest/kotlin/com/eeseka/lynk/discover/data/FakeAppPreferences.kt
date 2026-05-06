package com.eeseka.lynk.discover.data

import com.eeseka.lynk.shared.domain.settings.AppPreferences
import com.eeseka.lynk.shared.domain.settings.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAppPreferences : AppPreferences {
    private val themeFlow = MutableStateFlow(AppTheme.SYSTEM)
    override val theme: Flow<AppTheme> = themeFlow

    private val hasSeenOnboardingFlow = MutableStateFlow(false)
    override val hasSeenOnboarding: Flow<Boolean> = hasSeenOnboardingFlow

    override suspend fun setTheme(theme: AppTheme) {
        themeFlow.value = theme
    }

    override suspend fun setOnboardingCompleted() {
        hasSeenOnboardingFlow.value = true
    }
}