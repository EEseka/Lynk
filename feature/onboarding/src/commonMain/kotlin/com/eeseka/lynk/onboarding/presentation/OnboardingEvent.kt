package com.eeseka.lynk.onboarding.presentation

sealed interface OnboardingEvent {
    data object Success : OnboardingEvent
}