package com.eeseka.lynk.shared.design_system.components.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
actual fun rememberAppHaptic(): (AppHaptic) -> Unit {
    val hapticFeedback = LocalHapticFeedback.current
    val areHapticsEnabled = LocalHapticsEnabled.current

    return remember(hapticFeedback, areHapticsEnabled) {
        { hapticType ->
            if (areHapticsEnabled) {
                val materialType = when (hapticType) {
                    AppHaptic.Success -> HapticFeedbackType.Confirm
                    AppHaptic.Warning, AppHaptic.Error -> HapticFeedbackType.Reject
                    AppHaptic.Selection -> HapticFeedbackType.TextHandleMove
                    AppHaptic.ImpactLight -> HapticFeedbackType.ContextClick
                    AppHaptic.ImpactMedium, AppHaptic.ImpactHeavy -> HapticFeedbackType.LongPress
                }
                hapticFeedback.performHapticFeedback(materialType)
            }
        }
    }
}