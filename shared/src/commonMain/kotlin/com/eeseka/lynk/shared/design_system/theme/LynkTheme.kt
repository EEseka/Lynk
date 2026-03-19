package com.eeseka.lynk.shared.design_system.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.slapps.cupertino.theme.CupertinoTheme

@Composable
fun LynkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val lynkColors = if (darkTheme) LynkDarkColors else LynkLightColors

    CompositionLocalProvider(
        LocalLynkColors provides lynkColors,
        LocalLynkShapes provides LynkShapes()
    ) {
        if (isIOS()) {
            CupertinoLynkTheme(darkTheme = darkTheme, content = content)
        } else {
            MaterialLynkTheme(darkTheme = darkTheme, content = content)
        }
    }
}

@Composable
private fun MaterialLynkTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) MaterialDarkScheme else MaterialLightScheme,
        typography = getMaterialTypography(),
        content = content
    )
}

@Composable
private fun CupertinoLynkTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    CupertinoTheme(
        colorScheme = if (darkTheme) CupertinoDarkScheme else CupertinoLightScheme,
        typography = getCupertinoTypography(),
        content = content
    )
}