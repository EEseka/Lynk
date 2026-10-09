package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

internal actual fun hangoutMapDialogProperties(): DialogProperties {
    return DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = false,
        usePlatformInsets = false,
        useSoftwareKeyboardInset = false
    )
}

// Nothing to do: iOS colors the status bar from the window's style, which ApplyNativeTheme already sets from the app theme
@Composable
internal actual fun ThemedSystemBarIcons(isDark: Boolean) = Unit
