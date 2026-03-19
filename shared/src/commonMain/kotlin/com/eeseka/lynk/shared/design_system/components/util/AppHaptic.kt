package com.eeseka.lynk.shared.design_system.components.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.slapps.cupertino.CupertinoHapticFeedback
import com.slapps.cupertino.InternalCupertinoApi

/**
 * The Haptic Dictionary
 * These are the semantic physical feedbacks your app supports.
 * We abstract this so your UI never knows about platform-specific implementations.
 */
enum class AppHaptic {
    Success,
    Warning,
    Error,
    Selection,
    ImpactLight,
    ImpactMedium,
    ImpactHeavy
}

/**
 * The Unified Haptic Hook
 * Call this inside your Composables to get a platform-aware haptic trigger.
 */
@OptIn(InternalCupertinoApi::class)
@Composable
fun rememberAppHaptic(): (AppHaptic) -> Unit {
    val hapticFeedback = LocalHapticFeedback.current

    val isIosPlatform = isIOS()

    return remember(hapticFeedback, isIosPlatform) {
        { hapticType ->
            if (isIosPlatform) {
                // Map to the exact Cupertino haptics from the library
                val cupertinoType = when (hapticType) {
                    AppHaptic.Success -> CupertinoHapticFeedback.Success
                    AppHaptic.Warning -> CupertinoHapticFeedback.Warning
                    AppHaptic.Error -> CupertinoHapticFeedback.Error
                    AppHaptic.Selection -> CupertinoHapticFeedback.SelectionChanged
                    AppHaptic.ImpactLight -> CupertinoHapticFeedback.ImpactLight
                    AppHaptic.ImpactMedium -> CupertinoHapticFeedback.ImpactMedium
                    AppHaptic.ImpactHeavy -> CupertinoHapticFeedback.ImpactHeavy
                }
                hapticFeedback.performHapticFeedback(cupertinoType)
            } else {
                // Map to the closest Material 3 Android equivalents
                val materialType = when (hapticType) {
                    AppHaptic.Success -> HapticFeedbackType.Confirm
                    AppHaptic.Warning, AppHaptic.Error -> HapticFeedbackType.Reject
                    AppHaptic.Selection -> HapticFeedbackType.TextHandleMove
                    AppHaptic.ImpactLight -> HapticFeedbackType.ContextClick
                    AppHaptic.ImpactMedium -> HapticFeedbackType.LongPress
                    AppHaptic.ImpactHeavy -> HapticFeedbackType.LongPress
                }
                hapticFeedback.performHapticFeedback(materialType)
            }
        }
    }
}