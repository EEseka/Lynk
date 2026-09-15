package com.eeseka.lynk.shared.design_system.theme

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.eeseka.lynk.shared.domain.settings.AppTheme

// The navigation bar scrims enableEdgeToEdge uses by default; only API 28 and below draw them.
private val LIGHT_NAVIGATION_SCRIM = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val DARK_NAVIGATION_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b)

@Composable
actual fun ApplyNativeTheme(theme: AppTheme) {
    val activity = LocalActivity.current as? ComponentActivity ?: return

    LaunchedEffect(theme) {
        when (theme) {
            AppTheme.SYSTEM -> activity.enableEdgeToEdge()
            AppTheme.LIGHT -> activity.enableEdgeToEdgeFor(isDark = false)
            AppTheme.DARK -> activity.enableEdgeToEdgeFor(isDark = true)
        }
    }
}

private fun ComponentActivity.enableEdgeToEdgeFor(isDark: Boolean) {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.auto(
            lightScrim = Color.TRANSPARENT,
            darkScrim = Color.TRANSPARENT,
            detectDarkMode = { isDark }
        ),
        navigationBarStyle = SystemBarStyle.auto(
            lightScrim = LIGHT_NAVIGATION_SCRIM,
            darkScrim = DARK_NAVIGATION_SCRIM,
            detectDarkMode = { isDark }
        )
    )
}
