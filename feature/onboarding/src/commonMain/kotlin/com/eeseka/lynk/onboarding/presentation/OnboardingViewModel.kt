package com.eeseka.lynk.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.shared.domain.settings.AppPreferences
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val appPreferences: AppPreferences
) : ViewModel() {
    private val eventChannel = Channel<OnboardingEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.OnGetStartedClick -> onGetStartedClick()
        }
    }

    private fun onGetStartedClick() {
        viewModelScope.launch {
            appPreferences.setOnboardingCompleted()
            eventChannel.send(OnboardingEvent.Success)
        }
    }
}