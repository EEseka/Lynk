package com.eeseka.lynk.core.design_system.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.uikit.LocalUIViewController
import com.eeseka.lynk.core.domain.settings.AppTheme
import platform.UIKit.UIUserInterfaceStyle

@Composable
actual fun ApplyNativeTheme(theme: AppTheme) {
    val viewController = LocalUIViewController.current

    LaunchedEffect(theme) {
        viewController.view.window?.overrideUserInterfaceStyle = when (theme) {
            AppTheme.SYSTEM -> UIUserInterfaceStyle.UIUserInterfaceStyleUnspecified
            AppTheme.LIGHT -> UIUserInterfaceStyle.UIUserInterfaceStyleLight
            AppTheme.DARK -> UIUserInterfaceStyle.UIUserInterfaceStyleDark
        }
    }
}
