package com.eeseka.lynk.shared.design_system.components.layouts

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable

actual val WindowInsets.Companion.softwareKeyboard: WindowInsets
    @Composable get() = ime
