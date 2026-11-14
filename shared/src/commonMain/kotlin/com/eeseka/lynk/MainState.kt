package com.eeseka.lynk

import com.eeseka.lynk.core.domain.auth.model.User
import com.eeseka.lynk.core.domain.settings.AppTheme

data class MainState(
    val isCheckingAuth: Boolean = true,
    val hasSeenOnboarding: Boolean = false,
    val theme: AppTheme = AppTheme.SYSTEM,
    val isPureBlackEnabled: Boolean = false,
    val areHapticsEnabled: Boolean = true,
    val user: User? = null
)