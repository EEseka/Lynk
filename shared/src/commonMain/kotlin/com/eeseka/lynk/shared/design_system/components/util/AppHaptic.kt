package com.eeseka.lynk.shared.design_system.components.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

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

// The Settings haptics switch, provided once in App.kt
val LocalHapticsEnabled = staticCompositionLocalOf { true }

/**
 * Unified haptic hook — returns a platform-aware trigger function.
 * Use inside any Composable: val haptic = rememberAppHaptic()
 * Then call: haptic(AppHaptic.Selection)
 */
@Composable
expect fun rememberAppHaptic(): (AppHaptic) -> Unit