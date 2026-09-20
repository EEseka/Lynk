package com.eeseka.lynk.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.shared.domain.onboarding.OnboardingStorage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val onboardingStorage: OnboardingStorage
) : ViewModel() {
    private val eventChannel = Channel<OnboardingEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.OnGetStartedClick -> completeOnboarding()
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            onboardingStorage.setOnboardingCompleted()
            eventChannel.send(OnboardingEvent.Success)
        }
    }
}