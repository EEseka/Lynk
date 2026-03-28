package com.eeseka.lynk

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.eeseka.lynk.onboarding.presentation.OnboardingScreen
import com.eeseka.lynk.shared.design_system.theme.LynkTheme

@Composable
@Preview
fun App() {
    LynkTheme {
        OnboardingScreen()
    }
}