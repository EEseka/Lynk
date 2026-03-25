package com.eeseka.lynk.shared.design_system.components.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType
import platform.UIKit.UISelectionFeedbackGenerator

@Composable
actual fun rememberAppHaptic(): (AppHaptic) -> Unit {
    // Cache the generators so the Taptic Engine stays warm and doesn't
    // need to re-initialize hardware states on every tap.
    val notificationGenerator = remember { UINotificationFeedbackGenerator() }
    val selectionGenerator = remember { UISelectionFeedbackGenerator() }
    val lightImpactGenerator =
        remember { UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight) }
    val mediumImpactGenerator =
        remember { UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium) }
    val heavyImpactGenerator =
        remember { UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy) }

    return remember {
        { hapticType ->
            when (hapticType) {
                AppHaptic.Success -> {
                    notificationGenerator.prepare()
                    notificationGenerator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
                }

                AppHaptic.Warning -> {
                    notificationGenerator.prepare()
                    notificationGenerator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeWarning)
                }

                AppHaptic.Error -> {
                    notificationGenerator.prepare()
                    notificationGenerator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeError)
                }

                AppHaptic.Selection -> {
                    selectionGenerator.prepare()
                    selectionGenerator.selectionChanged()
                }

                AppHaptic.ImpactLight -> {
                    lightImpactGenerator.prepare()
                    lightImpactGenerator.impactOccurred()
                }

                AppHaptic.ImpactMedium -> {
                    mediumImpactGenerator.prepare()
                    mediumImpactGenerator.impactOccurred()
                }

                AppHaptic.ImpactHeavy -> {
                    heavyImpactGenerator.prepare()
                    heavyImpactGenerator.impactOccurred()
                }
            }
        }
    }
}