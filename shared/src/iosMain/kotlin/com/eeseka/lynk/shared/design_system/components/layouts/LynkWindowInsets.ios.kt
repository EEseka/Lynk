package com.eeseka.lynk.shared.design_system.components.layouts

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import platform.Foundation.NSProcessInfo
import platform.Foundation.iOSAppOnMac

// A Mac never shows an on-screen keyboard, so whatever keyboard it reports is not one
private val isRunningOnMac = NSProcessInfo.processInfo.iOSAppOnMac

actual val WindowInsets.Companion.softwareKeyboard: WindowInsets
    @Composable get() = if (isRunningOnMac) WindowInsets(0) else ime
