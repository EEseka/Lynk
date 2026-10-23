package com.eeseka.lynk.shared.design_system.components.layouts

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable

/**
 * The on-screen keyboard. Use this instead of WindowInsets.ime: an iPad app running on a Mac is
 * told a keyboard is open when none is, which lifted screens and dialogs off the bottom.
 */
expect val WindowInsets.Companion.softwareKeyboard: WindowInsets
    @Composable get

/** WindowInsets.safeDrawing, but with [softwareKeyboard] in place of the raw keyboard inset. */
val WindowInsets.Companion.safeArea: WindowInsets
    @Composable get() = systemBars.union(displayCutout).union(softwareKeyboard)
