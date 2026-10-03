package com.eeseka.lynk.shared.design_system.components.modals_and_overlays

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.window.DialogProperties
import platform.UIKit.UIUserInterfaceStyle

internal actual fun fullScreenDialogProperties(): DialogProperties {
    return DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = false,
        usePlatformInsets = false
    )
}

@Composable
internal actual fun LightSystemBarIcons() {
    val viewController = LocalUIViewController.current
    // iOS colors the status bar from the window's style, so the window goes dark while this is open
    DisposableEffect(viewController) {
        val window = viewController.view.window
        val previousStyle = window?.overrideUserInterfaceStyle
        window?.overrideUserInterfaceStyle = UIUserInterfaceStyle.UIUserInterfaceStyleDark
        onDispose {
            if (previousStyle != null) window.overrideUserInterfaceStyle = previousStyle
        }
    }
}
