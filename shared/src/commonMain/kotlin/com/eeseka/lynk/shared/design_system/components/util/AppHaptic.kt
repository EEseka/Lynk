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
 * Semantic physical feedbacks your app supports — platform-agnostic.
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
 * Unified haptic hook — returns a platform-aware trigger function.
 * Use inside any Composable: val haptic = rememberAppHaptic()
 * Then call: haptic(AppHaptic.Selection)
 */
@OptIn(InternalCupertinoApi::class)
@Composable
fun rememberAppHaptic(): (AppHaptic) -> Unit {
    val hapticFeedback = LocalHapticFeedback.current
    val isIosPlatform = isIOS()

    return remember(hapticFeedback, isIosPlatform) {
        { hapticType ->
            if (isIosPlatform) {
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