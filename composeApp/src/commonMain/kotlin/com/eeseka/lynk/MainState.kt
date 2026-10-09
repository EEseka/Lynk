package com.eeseka.lynk

import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.settings.AppTheme

data class MainState(
    val isCheckingAuth: Boolean = true,
    val hasSeenOnboarding: Boolean = false,
    val theme: AppTheme = AppTheme.SYSTEM,
    val isPureBlackEnabled: Boolean = false,
    val areHapticsEnabled: Boolean = true,
    val user: User? = null
)