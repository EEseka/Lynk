package com.eeseka.lynk.shared.design_system.theme

import androidx.compose.runtime.Composable
import com.eeseka.lynk.shared.domain.settings.AppTheme

/**
 * Tells the platform's own views which theme the user picked.
 * LynkTheme only reaches Compose; native views follow the phone's setting unless told otherwise.
 */
@Composable
expect fun ApplyNativeTheme(theme: AppTheme)
