package com.eeseka.lynk.onboarding.presentation

sealed interface OnboardingAction {
    data object OnGetStartedClick : OnboardingAction
}