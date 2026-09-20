package com.eeseka.lynk.shared.domain.onboarding

import kotlinx.coroutines.flow.Flow

interface OnboardingStorage {
    val hasSeenOnboarding: Flow<Boolean>

    suspend fun setOnboardingCompleted()
}
