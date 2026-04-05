package com.eeseka.lynk

import com.eeseka.lynk.shared.domain.settings.AppTheme

data class MainState(
    val isLoggedIn: Boolean = false,
    val isCheckingAuth: Boolean = true,
    val theme: AppTheme = AppTheme.SYSTEM,
    val hasSeenOnboarding: Boolean = false
)
