package com.eeseka.lynk.shared.design_system.components.util

import androidx.compose.runtime.Composable

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
@Composable
expect fun rememberAppHaptic(): (AppHaptic) -> Unit